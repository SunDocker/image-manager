package cn.edu.hit.imagemanager;

import cn.edu.hit.imagemanager.util.HttpRequestSender;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;

@SpringBootTest
class ImageManagerApplicationTests {
    @Autowired
    HttpRequestSender httpRequestSender;

    private final Logger logger = LoggerFactory.getLogger(ImageManagerApplicationTests.class);

    @Test
    void contextLoads() {
    }

    @Test
    void requestImageTest() {
        for (int i = 0; i < 30; i++) {
            new Thread(() -> {
                String imageName = "image" + new Random().nextInt(10);
                String tName = Thread.currentThread().getName();
                logger.info(tName + "开始请求" + imageName);
                long beginTime = System.currentTimeMillis();
                String res = httpRequestSender.get("http://localhost:8080/hitminer/" + imageName, null);
                long endTime = System.currentTimeMillis();
                logger.info(tName + "得到请求" + imageName + "的结果：" + res.substring(0, 13) + "\t请求耗时：" + (endTime - beginTime) + "ms");
            }).start();
        }
        while (true) {

        }
    }

}
