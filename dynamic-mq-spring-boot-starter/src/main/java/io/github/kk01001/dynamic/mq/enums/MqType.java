package io.github.kk01001.dynamic.mq.enums;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description MQ类型枚举
 */
public enum MqType {
    
    /**
     * RocketMQ
     */
    ROCKETMQ("rocketmq", "Apache RocketMQ"),
    
    /**
     * RabbitMQ
     */
    RABBITMQ("rabbitmq", "RabbitMQ"),
    
    /**
     * Kafka
     */
    KAFKA("kafka", "Apache Kafka"),
    
    /**
     * Redis
     */
    REDIS("redis", "Redis Stream");
    
    private final String code;
    private final String description;
    
    MqType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static MqType fromCode(String code) {
        for (MqType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MQ type: " + code);
    }
}
