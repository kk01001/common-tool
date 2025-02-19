package io.github.kk01001.disruptor.config;

import com.lmax.disruptor.dsl.ProducerType;
import io.github.kk01001.disruptor.annotation.WaitStrategyType;
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
     * 默认缓冲区大小
     */
    private int bufferSize = 1024;

    /**
     * 默认生产者类型
     */
    private ProducerType producerType = ProducerType.MULTI;

    /**
     * 默认等待策略
     */
    private WaitStrategyType waitStrategy = WaitStrategyType.BLOCKING;
} 