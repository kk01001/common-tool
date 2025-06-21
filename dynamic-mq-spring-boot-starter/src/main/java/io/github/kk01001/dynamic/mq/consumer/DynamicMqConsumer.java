package io.github.kk01001.dynamic.mq.consumer;

import io.github.kk01001.dynamic.mq.model.MqMessage;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 动态MQ消费者接口
 */
public interface DynamicMqConsumer {
    
    /**
     * 消费消息
     *
     * @param messages 消息列表
     * @param ack 消息确认接口
     */
    void consume(List<MqMessage> messages, Acknowledgement ack);
    
    /**
     * 消费单条消息（默认实现）
     *
     * @param message 单条消息
     * @param ack 消息确认接口
     */
    default void consume(MqMessage message, Acknowledgement ack) {
        consume(List.of(message), ack);
    }
}
