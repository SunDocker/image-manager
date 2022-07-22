package cn.edu.hit.imagemanager.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ImageManager {
    // 最大线程数
    // TODO ---------------------------------- for test ----------------------------------
    private static final int MAX_THREAD_NUM = 5;

    //最大镜像数
    // TODO ---------------------------------- for test ----------------------------------
    //private static final int MAX_IMAGE_NUM = 50;
    private static final int MAX_IMAGE_NUM = 3;

    // HashMap加载因子
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    // HashMap初始容量（减少扩容次数）
    private static final int LOADED_MAP_INIT_CAPACITY = (int) Math.ceil(MAX_IMAGE_NUM / DEFAULT_LOAD_FACTOR);
    private static final int LOADING_MAP_INIT_CAPACITY = (int) Math.ceil(MAX_THREAD_NUM / DEFAULT_LOAD_FACTOR);

    // 存储已经下载过的image名称和下载完成的时间
    // 使用ConcurrentHashMap，允许并发访问，同时在这个场景下允许一定程度的数据不一致问题
//    private static final Map<String, Long> loadedImages = new HashMap<>(LOADED_MAP_INIT_CAPACITY);
    private static final ConcurrentMap<String, Long> loadedImages = new ConcurrentHashMap<>(LOADED_MAP_INIT_CAPACITY);

    // 存储正在下载的image名称，其容量和最大线程数保持一致
    private static final Set<String> loadingImages = new HashSet<>(LOADING_MAP_INIT_CAPACITY);

    // 线程池，控制线程数量
    private static final ExecutorService threadPoolForLoadImages = Executors.newFixedThreadPool(MAX_THREAD_NUM);
    private static final ExecutorService threadPoolForDeleteImages = Executors.newFixedThreadPool(MAX_THREAD_NUM);

    // 锁
    private static final Lock lockForCheckLoad = new ReentrantLock();
    private static final Lock lockForDeleteImage = new ReentrantLock();

    public static String loadImage(String imageName) {
        // 上锁，防止高并发时相同请求下载多次
        lockForCheckLoad.lock();
        // 检查loadedImage，确定之前是否已经下载过相同image
        if (loadedImages.containsKey(imageName)) {
            // 有相同的则释放锁，直接返回rocksdb中的结果
            // 更新时间
            loadedImages.put(imageName, System.currentTimeMillis());
            lockForCheckLoad.unlock();
            return RocksDBManager.readPipList();
        }
        // 没有相同的，则继续向下执行

        // 继续检查当前image是否在loadingImages中
        // TODO 取常量池中的字符串当锁，数据量大时可能会占用较多内存，频繁fullGC
        String imageLock = imageName.intern();
        if (loadingImages.contains(imageName)) {
            // 如果在则释放锁，wait，直到下载完被唤醒，然后读取rocksdb，返回执行结果
            synchronized (imageLock) {
                // 一定要在获得字符串锁之后再释放之前的锁，不然wait之后可能不会被notify
                lockForCheckLoad.unlock();
                try {
                    imageLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return RocksDBManager.readPipList();
            }
        }
        // 不在下载过程中，则标记为正在下载中
        loadingImages.add(imageName);
        // 释放锁，继续向下执行
        lockForCheckLoad.unlock();

        Future<String> loadImageTask = threadPoolForLoadImages.submit(() -> {
            // 执行shell命令，开始下载image
            // 要执行的指令
            // TODO ---------------------------------- for test ----------------------------------
            //String command = "docker run --rm --entrypoint bash " + imageName + " python -m pip list --format=freeze";
            //String command = "choice /t 5 /d y /n >nul & echo testing!!!";
            String command = "ping www.test.com";
            String pipList = null;
            pipList = ShellExecutor.execWithResult(command);
            // TODO ---------------------------------- for test ----------------------------------
            System.out.println(Thread.currentThread().getName() + "执行了一次拉取镜像" + imageName + "的command");
            // 下载完成
            return pipList;
        });
        String pipList = null;
        try {
            pipList = loadImageTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            // TODO 如果出错了返回什么？？？
            throw new RuntimeException("拉取image的线程出错");
        }

        // 注：下面这四步的顺序对于并发的相同请求来说很重要
        // 1.将新的执行结果保存到rocksdb中
        RocksDBManager.storePipList(pipList);
        // 2.更新loadedImages
        loadedImages.put(imageName, System.currentTimeMillis());
        //上锁，防止相同请求多次下载
        lockForCheckLoad.lock();
        // 3.从loadingImages中移除
        loadingImages.remove(imageName);
        //释放锁
        lockForCheckLoad.unlock();
        // 4.唤醒等待的线程
        synchronized (imageLock) {
            imageLock.notifyAll();
        }

        // 开新的线程去检查下载的image数量是否达到阈值
        threadPoolForDeleteImages.execute(() -> {
            // 多个线程之间串行删除
            lockForDeleteImage.lock();
            if (loadedImages.size() > MAX_IMAGE_NUM) {
                // 如果达到则删除旧的image
                // TODO 快速找到value最小的key
                long minTime = System.currentTimeMillis(); // 过去时间戳都小于当前时间戳
                String outdatedImage = null;
                boolean deleteFlag = true;
                do {
                    for (Map.Entry<String, Long> entryImage : loadedImages.entrySet()) {
                        if (entryImage.getValue() < minTime) {
                            minTime = entryImage.getValue();
                            outdatedImage = entryImage.getKey();
                        }
                    }
                    if (Objects.nonNull(outdatedImage)) {
                        lockForCheckLoad.lock();
                        // 检查时间是否更新，如果更新了则不能删除这个，要回去重新遍历集合
                        if (minTime < loadedImages.get(outdatedImage)) {
                            deleteFlag = false;
                        }
                        if (deleteFlag) {
                            loadedImages.remove(outdatedImage);
                        }
                        lockForCheckLoad.unlock();
                    }
                } while (Objects.nonNull(outdatedImage) && !deleteFlag);


                if (Objects.nonNull(outdatedImage)) {
                    // TODO docker删除镜像的语法允许使用"镜像名:版本号"，是否可以直接从输入中得到该信息？还是要再执行"docker images -q"查询id？
                    String[] splitImage = outdatedImage.split("/");
                    String imageNameAndVersion = splitImage[splitImage.length - 1];
                    // TODO ---------------------------------- for test ----------------------------------
                    //String command = "docker rmi " + imageNameAndVersion;
                    String command = "echo " + outdatedImage + ':' + minTime;
                    String delResult = ShellExecutor.execWithResult(command);
                    System.out.print("异步删除" + delResult + "  ");
                }
            }
            lockForDeleteImage.unlock();
        });

        // 返回新的执行结果
        return pipList;
    }


}
