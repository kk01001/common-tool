package io.github.kk01001.dynamic.mq.core;

import io.github.kk01001.dynamic.mq.model.MqMessage;
import io.github.kk01001.dynamic.mq.model.MqSendResult;
import lombok.extern.slf4j.Slf4j;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description MQ生产者抽象类，使用模板方法模式
 */
@Slf4j
public abstract class AbstractMqProducer implements MqProducer {
    
    /**
     * 发送消息模板方法
     */
    @Override
    public final boolean send(MqMessage message) {
        try {
            // 前置处理
            preProcess(message);
            
            // 参数校验
            validateMessage(message);
            
            // 执行发送
            boolean result = doSend(message);
            
            // 后置处理
            postProcess(message, result);
            
            return result;
        } catch (Exception e) {
            log.error("发送消息失败: {}", message, e);
            handleException(message, e);
            return false;
        }
    }
    
    /**
     * 发送延迟消息模板方法
     */
    @Override
    public final boolean sendDelay(MqMessage message, long delayTime) {
        try {
            message.setDelayTime(delayTime);
            return send(message);
        } catch (Exception e) {
            log.error("发送延迟消息失败: {}", message, e);
            return false;
        }
    }
    
    /**
     * 批量发送消息模板方法
     */
    @Override
    public final boolean sendBatch(MqMessage... messages) {
        try {
            return doBatchSend(messages);
        } catch (Exception e) {
            log.error("批量发送消息失败", e);
            return false;
        }
    }
    
    /**
     * 前置处理
     */
    protected void preProcess(MqMessage message) {
        if (message.getCreateTime() == null) {
            message.setCreateTime(System.currentTimeMillis());
        }
        if (message.getMessageId() == null) {
            message.setMessageId(generateMessageId());
        }
    }
    
    /**
     * 后置处理
     */
    protected void postProcess(MqMessage message, boolean result) {
        if (result) {
            log.debug("消息发送成功: {}", message.getMessageId());
        } else {
            log.warn("消息发送失败: {}", message.getMessageId());
        }
    }
    
    /**
     * 异常处理
     */
    protected void handleException(MqMessage message, Exception e) {
        log.error("消息处理异常: messageId={}, error={}", message.getMessageId(), e.getMessage());
    }
    
    /**
     * 消息校验
     */
    protected void validateMessage(MqMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("消息不能为空");
        }
        if (message.getTopic() == null || message.getTopic().trim().isEmpty()) {
            throw new IllegalArgumentException("主题不能为空");
        }
        if (message.getPayload() == null) {
            throw new IllegalArgumentException("消息体不能为空");
        }
    }
    
    /**
     * 生成消息ID
     */
    protected String generateMessageId() {
        return System.currentTimeMillis() + "-" + Thread.currentThread().threadId();
    }
    
    // 抽象方法，由子类实现
    
    /**
     * 执行发送消息
     */
    protected abstract boolean doSend(MqMessage message);
    
    /**
     * 执行批量发送
     */
    protected abstract boolean doBatchSend(MqMessage... messages);

    // ==================== 新增方法的默认实现 ====================

    @Override
    public void sendAsync(MqMessage message, MqSendCallback callback) {
        // 默认实现：使用同步发送模拟异步
        try {
            boolean success = send(message);
            if (success) {
                MqSendResult result = MqSendResult.success(message.getMessageId(), message.getTopic());
                callback.onSuccess(result);
            } else {
                callback.onException(new RuntimeException("发送失败"));
            }
        } catch (Exception e) {
            callback.onException(e);
        }
    }

    @Override
    public void sendOneWay(MqMessage message) {
        // 默认实现：使用同步发送，忽略结果
        try {
            send(message);
        } catch (Exception e) {
            log.warn("OneWay消息发送失败，忽略异常: {}", e.getMessage());
        }
    }

    @Override
    public boolean sendOrderly(MqMessage message, String orderKey) {
        // 默认实现：使用普通发送
        log.warn("当前MQ实现不支持顺序消息，使用普通发送: {}", getProducerType());
        return send(message);
    }

    @Override
    public boolean sendTransaction(MqMessage message, String transactionId, MqTransactionListener transactionListener) {
        log.warn("当前MQ实现不支持事务消息: {}", getProducerType());
        return false;
    }
}
