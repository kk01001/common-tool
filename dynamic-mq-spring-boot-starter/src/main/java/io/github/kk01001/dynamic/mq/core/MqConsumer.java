package io.github.kk01001.dynamic.mq.core;

import io.github.kk01001.dynamic.mq.model.MqMessage;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 消息消费者统一接口
 */
public interface MqConsumer {
    
    /**
     * 启动消费者
     */
    void start();
    
    /**
     * 停止消费者
     */
    void stop();
    
    /**
     * 订阅主题
     *
     * @param topic 主题名称
     * @param tag 标签
     * @param messageHandler 消息处理器
     */
    void subscribe(String topic, String tag, MqMessageHandler messageHandler);
    
    /**
     * 取消订阅
     *
     * @param topic 主题名称
     */
    void unsubscribe(String topic);
    
    /**
     * 获取消费者类型
     *
     * @return 消费者类型
     */
    String getConsumerType();
    
    /**
     * 消息处理器接口
     */
    @FunctionalInterface
    interface MqMessageHandler {
        
        /**
         * 处理消息
         *
         * @param message 消息对象
         * @return 处理结果，true表示成功，false表示失败需要重试
         */
        boolean handle(MqMessage message);
    }
}
