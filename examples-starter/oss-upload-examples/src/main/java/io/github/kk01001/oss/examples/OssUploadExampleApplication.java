package io.github.kk01001.oss.examples;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

/**
 * @author linshiqiang
 * @date 2025-11-14 10:26:30
 * @description 示例应用入口
 */
@SpringBootApplication
public class OssUploadExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(OssUploadExampleApplication.class, args);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofGigabytes(1));
        factory.setMaxRequestSize(DataSize.ofGigabytes(1));
        factory.setFileSizeThreshold(DataSize.ofMegabytes(1));
        return factory.createMultipartConfig();
    }
}