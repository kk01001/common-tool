package io.github.archer099.excel.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author archer099
 * @date 2026-01-22 10:00:00
 * @description Excel 线程池配置类
 */
@Configuration
@ConfigurationProperties(prefix = "excel.thread-pool")
public class ExcelThreadPoolConfig {

    /**
     * 创建 Excel 处理专用线程池（虚拟线程）
     *
     * @return Excel 线程池
     */
    @Bean("excelThreadPool")
    @ConditionalOnMissingBean(name = "excelThreadPool")
    public ExecutorService excelThreadPool() {
        ThreadFactory namedThreadFactory = Thread.ofVirtual()
                .name("excel-pool-", 0)
                .factory();
        return Executors.newThreadPerTaskExecutor(namedThreadFactory);
    }

}