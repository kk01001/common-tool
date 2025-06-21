package io.github.kk01001.dynamic.mq.enums;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 消息模型枚举
 */
public enum MessageModel {
    
    /**
     * 未设置，使用默认配置
     */
    UNSET,
    
    /**
     * 集群模式（同一消费组内只有一个消费者消费消息）
     */
    CLUSTERING,
    
    /**
     * 广播模式（同一消费组内所有消费者都消费消息）
     */
    BROADCASTING
}
