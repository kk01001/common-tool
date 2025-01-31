package io.github.kk01001.excel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
@ConfigurationProperties(prefix = "excel.thread-pool")
public class ExcelThreadPoolConfig {

    @Bean("excelThreadPool")
    public ExecutorService excelThreadPool() {
        ThreadFactory namedThreadFactory = Thread.ofVirtual()
                .name("excelThreadPool-", 0)
                .factory();
       return Executors.newThreadPerTaskExecutor(namedThreadFactory);
    }

}