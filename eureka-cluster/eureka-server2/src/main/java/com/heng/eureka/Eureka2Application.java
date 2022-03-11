package com.heng.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/****
 * @Author:henzhang
 * @Description:
 *****/
@SpringBootApplication
@EnableEurekaServer //开启Eureka服务
public class Eureka2Application {
    /**
     * 加载启动类
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Eureka2Application.class, args);
    }
}
