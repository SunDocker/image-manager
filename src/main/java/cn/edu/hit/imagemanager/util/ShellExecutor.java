package cn.edu.hit.imagemanager.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class ShellExecutor {
    public static String execWithResult(String cmd) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        Process process = null;
        BufferedReader input = null;
        StringBuilder execResult = new StringBuilder();
        try {
            String prefix1 = isWindows ? "cmd" : "/bin/sh";
            String prefix2 = isWindows ? "/c" : "-c";
            String charsetName = isWindows ? "GB2312" : "UTF-8";
            process = Runtime.getRuntime().exec(new String[]{prefix1, prefix2, cmd});
            input = new BufferedReader(new InputStreamReader(process.getInputStream(), charsetName));
            String line = "";
            while ((line = input.readLine()) != null) {
                execResult.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
            // TODO 如果执行出错了返回什么？？？
        } finally {
            if (Objects.nonNull(input)) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if (Objects.isNull(process) || process.waitFor() != 0) {
                // TODO 如果执行出错了返回什么？？？
                return "error:" + execResult;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            // TODO 如果执行出错了返回什么？？？
        }
        return execResult.toString();
    }
}
