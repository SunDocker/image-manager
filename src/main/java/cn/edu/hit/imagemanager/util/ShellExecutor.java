package cn.edu.hit.imagemanager.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class ShellExecutor {
    /*public static void main(String[] args) {
//        String cmd = "docker run --rm --entrypoint bash nginx python -m pip list";
//        String cmd = "echo hello";
        String cmd = "python -m pip list";
        System.out.println(execWithResult(cmd));

    }*/
    public static String execWithResult(String cmd) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        Process process = null;
        BufferedReader input = null;
        StringBuilder execResult = new StringBuilder();
        String prefix1 = isWindows ? "cmd" : "/bin/sh";
        String prefix2 = isWindows ? "/c" : "-c";
        String charsetName = isWindows ? "GB2312" : "UTF-8";
        try {
            process = Runtime.getRuntime().exec(new String[]{prefix1, prefix2, cmd});
            if (process.waitFor() != 0) {
                // TODO 如果执行出错了返回什么？？？
                input = new BufferedReader(new InputStreamReader(process.getErrorStream(), charsetName));
            } else {
                input = new BufferedReader(new InputStreamReader(process.getInputStream(), charsetName));
            }
            String line = "";
            while ((line = input.readLine()) != null) {
                execResult.append(line).append('\n');
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // TODO 如果执行出错了返回什么？？？
            throw new RuntimeException("执行shell出错");
        } finally {
            if (Objects.nonNull(input)) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return execResult.toString();
    }
}
