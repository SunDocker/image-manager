package cn.edu.hit.imagemanager.util;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class RocksDBManager {
    // 要提前创建好
    private static final String DB_PATH = "/rocksdb/defaultCF";

//    private static final String PIP_LIST_KEY = "pipList";

    private static final RocksDB rocksDB;

    static {
        RocksDB rocksDB0;
        Options options = new Options().setCreateIfMissing(true);
        try {
            rocksDB0 = RocksDB.open(options, DB_PATH);
        } catch (RocksDBException e) {
            rocksDB0 = null;
            e.printStackTrace();
        }
        rocksDB = rocksDB0;
    }

    public static void storePipList(String pipKey, String pipList) {
        if (Objects.isNull(rocksDB)) {
            // TODO 如果出错了返回什么？？？
            throw new RuntimeException("rocksdb数据库初始化出错");
        }
        byte[] key = pipKey.getBytes(StandardCharsets.UTF_8);
        byte[] value = pipList.getBytes(StandardCharsets.UTF_8);
        try {
            rocksDB.put(key, value);
        } catch (RocksDBException e) {
            e.printStackTrace();
            // TODO 存储出错
            throw new RuntimeException("rocksdb存储出错");
        }
    }

    public static String readPipList(String pipKey) {
        if (Objects.isNull(rocksDB)) {
            // TODO 如果出错了返回什么？？？
            throw new RuntimeException("rocksdb数据库初始化出错");
        }
        byte[] key = pipKey.getBytes(StandardCharsets.UTF_8);
        byte[] value = null;
        try {
            value = rocksDB.get(key);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        if (Objects.isNull(value)) {
            // TODO 如果出错了返回什么？？？
            throw new RuntimeException("rocksdb读取出错");
        }
        return new String(value);
    }
}
