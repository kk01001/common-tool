package io.github.kk01001.dynamic.mq.core;

import io.github.kk01001.dynamic.mq.model.MqMessage;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 消息生产者统一接口
 */
public interface MqProducer {
    
    /**
     * 发送消息
     *
     * @param message 消息对象
     * @return 发送结果
     */
    boolean send(MqMessage message);
    
    /**
     * 发送延迟消息
     *
     * @param message 消息对象
     * @param delayTime 延迟时间（毫秒）
     * @return 发送结果
     */
    boolean sendDelay(MqMessage message, long delayTime);
    
    /**
     * 发送异步消息
     *
     * @param message 消息对象
     * @param callback 异步回调
     */
    void sendAsync(MqMessage message, MqSendCallback callback);

    /**
     * 发送OneWay消息（不等待响应）
     *
     * @param message 消息对象
     */
    void sendOneWay(MqMessage message);

    /**
     * 发送顺序消息
     *
     * @param message 消息对象
     * @param orderKey 顺序键，相同orderKey的消息保证顺序
     * @return 发送结果
     */
    boolean sendOrderly(MqMessage message, String orderKey);

    /**
     * 发送事务消息
     *
     * @param message 消息对象
     * @param transactionId 事务ID
     * @param transactionListener 事务监听器
     * @return 发送结果
     */
    boolean sendTransaction(MqMessage message, String transactionId, MqTransactionListener transactionListener);
    
    /**
     * 批量发送消息
     *
     * @param messages 消息列表
     * @return 发送结果
     */
    boolean sendBatch(MqMessage... messages);
    
    /**
     * 获取生产者类型
     *
     * @return 生产者类型
     */
    String getProducerType();
}
