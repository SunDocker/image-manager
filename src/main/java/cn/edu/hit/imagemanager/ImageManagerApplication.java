package cn.edu.hit.imagemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
// 必须部署到tomcat9上，否则无法启动spring
// 必须继承SpringBootServletInitializer
public class ImageManagerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ImageManagerApplication.class, args);
    }
}
