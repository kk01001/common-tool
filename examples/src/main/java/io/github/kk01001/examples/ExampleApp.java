package io.github.kk01001.examples;

import io.github.kk01001.common.log.annotation.EnableOperationLog;
import io.github.kk01001.crypto.annotation.EnableParamsCrypto;
import io.github.kk01001.examples.log.CustomOperationLogHandler;
import io.github.kk01001.examples.log.SecurityOperatorInfoProvider;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

/**
 * @author kk01001
 * date 2024-06-23 15:41:00
 */
@EnableOperationLog(
        handler = CustomOperationLogHandler.class,
        provider = SecurityOperatorInfoProvider.class
)
@EnableParamsCrypto
@SpringBootApplication
public class ExampleApp {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApp.class, args);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(1000)); // 单个文件最大 10MB
        factory.setMaxRequestSize(DataSize.ofMegabytes(1000)); // 总请求最大 50MB
        return factory.createMultipartConfig();
    }
}
