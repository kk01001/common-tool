package io.github.kk01001.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author kk01001
 * @date 2025-01-08 16:10:00
 * @description
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("io.github.kk01001.order.mapper")
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}