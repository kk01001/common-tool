package io.github.kk01001.disruptor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description Disruptor配置属性类
 */
@Data
@ConfigurationProperties(prefix = "disruptor")
public class DisruptorProperties {
    /**
     * 环形缓冲区大小，必须是2的幂
     */
    private int bufferSize = 1024;
    
    /**
     * 消费者线程数
     */
    private int consumerCount = 1;
} 