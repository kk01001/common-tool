package io.github.archer099.excel.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("io.github.archer099.excel")
@EnableConfigurationProperties(ExcelProperties.class)
public class ExcelAutoConfiguration {
    // 可以添加一些通用的配置
} 