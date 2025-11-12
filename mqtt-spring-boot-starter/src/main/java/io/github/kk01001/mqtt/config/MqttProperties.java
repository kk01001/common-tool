package io.github.kk01001.mqtt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * MQTT 配置属性
 *
 * @author kk01001
 */
@Data
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    /**
     * 是否启用 MQTT
     */
    private Boolean enabled = false;

    /**
     * MQTT Broker 地址，例如：tcp://localhost:1883 或 ssl://localhost:8883
     */
    private String brokerUrl;

    /**
     * 客户端 ID，如果为空则自动生成
     */
    private String clientId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否清除会话
     */
    private Boolean cleanSession = true;

    /**
     * 连接超时时间（秒）
     */
    private Duration connectionTimeout = Duration.ofSeconds(30);

    /**
     * 保持连接时间间隔（秒）
     */
    private Duration keepAliveInterval = Duration.ofSeconds(60);

    /**
     * 是否自动重连
     */
    private Boolean automaticReconnect = true;

    /**
     * 最大重连延迟时间（秒）
     */
    private Duration maxReconnectDelay = Duration.ofSeconds(128);

    /**
     * 遗嘱消息配置
     */
    private WillMessage will;

    /**
     * 生产者配置
     */
    private Producer producer = new Producer();

    /**
     * 消费者配置
     */
    private Consumer consumer = new Consumer();

    /**
     * SSL/TLS 配置
     */
    private MqttSslProperties ssl = new MqttSslProperties();

    /**
     * 遗嘱消息配置
     */
    @Data
    public static class WillMessage {
        /**
         * 遗嘱主题
         */
        private String topic;

        /**
         * 遗嘱消息内容
         */
        private String payload;

        /**
         * 遗嘱消息 QoS
         */
        private Integer qos = 1;

        /**
         * 是否保留遗嘱消息
         */
        private Boolean retained = false;
    }

    /**
     * 生产者配置
     */
    @Data
    public static class Producer {
        /**
         * 默认 QoS
         */
        private Integer defaultQos = 1;

        /**
         * 默认是否保留消息
         */
        private Boolean defaultRetained = false;

        /**
         * 发送超时时间（毫秒）
         */
        private Duration sendTimeout = Duration.ofSeconds(5);

        /**
         * 异步发送
         */
        private Boolean async = false;
    }

    /**
     * 消费者配置
     */
    @Data
    public static class Consumer {
        /**
         * 默认订阅的主题，多个用逗号分隔
         */
        private String[] topics;

        /**
         * 默认 QoS
         */
        private Integer defaultQos = 1;

        /**
         * 消费线程池大小
         */
        private Integer threadPoolSize = 10;

        /**
         * 消费线程池队列大小
         */
        private Integer queueCapacity = 1000;
    }
}
