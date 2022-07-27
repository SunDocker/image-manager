package cn.edu.hit.imagemanager.util;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShellExecutor {
    private final static Logger logger = LoggerFactory.getLogger(ShellExecutor.class);

    public static void main(String[] args) {

    }

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

    public static String execWithEnvInLinux(String cmdWithEnv, Map<String, String> cmdEnv) {
        BufferedReader input = null;
        StringBuilder execResult = new StringBuilder();
        try {
            String[] cmd = {"bash", "-c", cmdWithEnv};
            Map<String, String> env = new HashMap<>(System.getenv());
            env.put("TERM", "xterm");
            cmdEnv.keySet().forEach(key -> env.put(key, cmdEnv.get(key)));
            PtyProcess process = new PtyProcessBuilder().setCommand(cmd).setEnvironment(env).start();
            if (process.waitFor() != 0) {
                // TODO 如果执行出错了返回什么？？？
                logger.error("指令执行错误");
//                input = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
                input = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            } else {
                logger.error("指令执行正确");
                input = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            }
            String line = "";
            while ((line = input.readLine()) != null) {
                execResult.append(line).append('\n');
            }
            logger.error("指令执行结果:" + execResult);
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
