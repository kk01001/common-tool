package io.github.kk01001.chat.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author kk01001
 * @date 2026-03-07 17:30:00
 * @description Netty WebSocket 聊天室示例
 */
@SpringBootApplication
@MapperScan("io.github.kk01001.chat.example.mapper")
public class NettyChatExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyChatExampleApplication.class, args);
    }
}
