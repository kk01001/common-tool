package io.github.kk01001.examples;

import io.github.kk01001.common.log.annotation.EnableOperationLog;
import io.github.kk01001.examples.log.CustomOperationLogHandler;
import io.github.kk01001.examples.log.SecurityOperatorInfoProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author kk01001
 * date 2024-06-23 15:41:00
 */
@EnableOperationLog(
        handler = CustomOperationLogHandler.class,
        provider = SecurityOperatorInfoProvider.class
)
@SpringBootApplication
public class ExampleApp {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApp.class, args);
    }
}
