package io.github.kk01001.sse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description SSE配置属性
 */
@Data
@ConfigurationProperties(prefix = "sse")
public class SseProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 集群模式
     */
    private boolean cluster = false;

    /**
     * 消息转发方式：redis, rocketmq, none
     */
    private String messageForwardType = "none";

    /**
     * 心跳间隔（毫秒）
     */
    private long heartbeatInterval = 10000;

    /**
     * 客户端超时时间（毫秒）
     */
    private long clientTimeout = 0L;

    /**
     * Redis配置
     */
    private Redis redis = new Redis();

    /**
     * RocketMQ配置
     */
    private RocketMq rocketMq = new RocketMq();

    /**
     * Redis配置
     */
    @Data
    public static class Redis {

        /**
         * Redis主题
         */
        private String topic = "sse:message";
    }

    /**
     * RocketMQ配置
     */
    @Data
    public static class RocketMq {

        /**
         * 主题
         */
        private String topic = "sse-message";

        /**
         * 消费者组
         */
        private String consumerGroup = "sse-consumer-group";

        /**
         * 消费者线程数
         */
        private int consumerCount = 1;
    }
}
