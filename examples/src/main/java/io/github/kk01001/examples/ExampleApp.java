package io.github.kk01001.examples;

import io.github.kk01001.core.EnableXxlJob;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author linshiqiang
 * date 2024-06-23 15:41:00
 */
@EnableXxlJob
@SpringBootApplication
public class ExampleApp {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApp.class, args);
    }
}
