package cn.edu.hit.imagemanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Component
public class HttpRequestSender {

    private final Logger logger = LoggerFactory.getLogger(HttpRequestSender.class);

    /**
     * HttpURLConnection Get 方式请求
     */
    public String get(String urlStr, Map<String, String> map) {

        StringBuilder result = new StringBuilder();   //StringBuilder用于单线程多字符串拼接，返回参数

        StringBuilder pathString = new StringBuilder(urlStr);
        pathString.append("?");
        pathString.append(getStringFromEntry(map));

        // 以下是 HttpURLConnection Get 访问 代码
        try {
            // 第一步 包装网络地址
            URL url = new URL(pathString.toString());
            // 第二步 创建连接对象
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            // 第三步 设置请求方式Get
            httpURLConnection.setRequestMethod("GET");
            // 第四步 设置读取和连接超时时长
            httpURLConnection.setReadTimeout(0);
            httpURLConnection.setConnectTimeout(5000);
            // 第五步 发生请求 ⚠注意：只有在httpURLConnection.getResponseCode()非-1时，才向服务器发请求
            int responseCode = httpURLConnection.getResponseCode();
            // 第六步 判断请求码是否成功  注意：只有在执行conn.getResponseCode() 的时候才开始向服务器发送请求
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 第七步 获取服务器响应的流
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String temp;
                while ((temp = reader.readLine()) != null) {
                    result.append(temp);
                }
            } else {
                return "failed";
            }
            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            logger.error("登录失败，请检查网络！");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO发生异常");
        }
//        logger.info(result.toString());
        return result.toString();
    }

    /**
     * HttpURLConnection Post式请求
     */
    public String post(String urlStr, Map<String, String> map) {

        StringBuilder result = new StringBuilder();  //StringBuilder用于单线程多字符串拼接，返回参数
        String paramsString = getStringFromEntry(map);  //获取拼接参数：name=admin&pwd=123456

        // 以下是 HttpURLConnection Post 访问 代码
        try {
            // 第一步 包装网络地址
            URL url = new URL(urlStr);
            // 第二步 创建连接对象
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 第三步 设置请求方式 POST
            conn.setRequestMethod("POST");
            // 第四步 设置读取和连接超时时长
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            // 第五步 允许对外输出
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(true);
            // 第六步 得到输出流 并把实体输出写出去
            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            writer.write(paramsString);
            writer.flush();
            writer.close();
            outputStream.close();
            // 第七步 判断请求码是否成功 注意：只有在执行conn.getResponseCode() 的时候才开始向服务器发送请求
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 第八步 获取服务器响应的流
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String temp;
                while ((temp = reader.readLine()) != null) {
                    result.append(temp);
                }
            } else {
                return "failed";
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            logger.error("登录失败，请检查网络！");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO发生异常");
        }
        return result.toString();
    }

    /**
     * 将map转换成key1=value1&key2=value2的形式
     */
    private String getStringFromEntry(Map<String, String> map) {
        if (Objects.isNull(map)) {
            return null;
        }

        StringBuilder sb = new StringBuilder(); //StringBuilder用于单线程多字符串拼接
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (isFirst)
                isFirst = false;
            else
                sb.append("&");
            sb.append(URLEncoder.encode(entry.getKey()));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue()));
        }
        return sb.toString();
    }
}
