package com.javalearn.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;


@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        //        SpringApplication.run(Application.class);
        SpringApplication application = new SpringApplication(Application.class);

        // 方式：在启动前将 .env 文件加载为 PropertySource
        application.addInitializers(context -> {
            try {
                ResourcePropertySource ps = new ResourcePropertySource("env", new FileSystemResource(".env"));
                context.getEnvironment().getPropertySources().addFirst(ps);
            } catch (IOException e) {
                // .env 不存在时的处理
            }
        });

        application.run(args);
    }
}
