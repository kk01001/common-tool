package io.github.kk01001.dynamic.mq.util;

import io.github.kk01001.dynamic.mq.model.MqMessage;
import cn.hutool.core.util.IdUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description MQ工具类
 */
public class MqUtils {
    
    /**
     * 构建简单消息
     *
     * @param topic 主题
     * @param payload 消息体
     * @return MQ消息
     */
    public static MqMessage buildSimpleMessage(String topic, Object payload) {
        return MqMessage.builder()
                .messageId(generateMessageId())
                .topic(topic)
                .payload(payload)
                .createTime(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 构建带标签的消息
     *
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息体
     * @return MQ消息
     */
    public static MqMessage buildTaggedMessage(String topic, String tag, Object payload) {
        return MqMessage.builder()
                .messageId(generateMessageId())
                .topic(topic)
                .tag(tag)
                .payload(payload)
                .createTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 构建消息（兼容示例代码）
     *
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息体
     * @return MQ消息
     */
    public static MqMessage buildMessage(String topic, String tag, String payload) {
        return MqMessage.builder()
                .messageId(generateMessageId())
                .topic(topic)
                .tag(tag)
                .payload(payload)
                .createTime(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 构建延迟消息
     *
     * @param topic 主题
     * @param payload 消息体
     * @param delayTime 延迟时间（毫秒）
     * @return MQ消息
     */
    public static MqMessage buildDelayMessage(String topic, Object payload, long delayTime) {
        return MqMessage.builder()
                .messageId(generateMessageId())
                .topic(topic)
                .payload(payload)
                .delayTime(delayTime)
                .createTime(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 构建带头信息的消息
     *
     * @param topic 主题
     * @param payload 消息体
     * @param headers 头信息
     * @return MQ消息
     */
    public static MqMessage buildMessageWithHeaders(String topic, Object payload, Map<String, Object> headers) {
        return MqMessage.builder()
                .messageId(generateMessageId())
                .topic(topic)
                .payload(payload)
                .headers(headers)
                .createTime(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 构建完整消息
     *
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息体
     * @param delayTime 延迟时间
     * @param priority 优先级
     * @param maxRetryCount 最大重试次数
     * @return MQ消息
     */
    public static MqMessage buildFullMessage(String topic, String tag, Object payload, 
                                           Long delayTime, Integer priority, Integer maxRetryCount) {
        Map<String, Object> headers = new HashMap<>();
        if (priority != null) {
            headers.put("priority", priority);
        }
        
        return MqMessage.builder()
                .messageId(generateMessageId())
                .topic(topic)
                .tag(tag)
                .payload(payload)
                .headers(headers)
                .delayTime(delayTime)
                .priority(priority)
                .maxRetryCount(maxRetryCount)
                .retryCount(0)
                .createTime(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 生成消息ID
     *
     * @return 消息ID
     */
    public static String generateMessageId() {
        return IdUtil.fastSimpleUUID();
    }
    
    /**
     * 检查消息是否有效
     *
     * @param message 消息
     * @return 是否有效
     */
    public static boolean isValidMessage(MqMessage message) {
        if (message == null) {
            return false;
        }
        
        if (message.getTopic() == null || message.getTopic().trim().isEmpty()) {
            return false;
        }
        
        if (message.getPayload() == null) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 复制消息（用于重试等场景）
     *
     * @param original 原始消息
     * @return 复制的消息
     */
    public static MqMessage copyMessage(MqMessage original) {
        if (original == null) {
            return null;
        }
        
        return MqMessage.builder()
                .messageId(original.getMessageId())
                .topic(original.getTopic())
                .tag(original.getTag())
                .payload(original.getPayload())
                .headers(original.getHeaders() != null ? new HashMap<>(original.getHeaders()) : null)
                .delayTime(original.getDelayTime())
                .priority(original.getPriority())
                .createTime(original.getCreateTime())
                .retryCount(original.getRetryCount())
                .maxRetryCount(original.getMaxRetryCount())
                .build();
    }
    
    /**
     * 增加重试次数
     *
     * @param message 消息
     * @return 更新后的消息
     */
    public static MqMessage incrementRetryCount(MqMessage message) {
        if (message == null) {
            return null;
        }
        
        int currentRetryCount = message.getRetryCount() != null ? message.getRetryCount() : 0;
        message.setRetryCount(currentRetryCount + 1);
        
        return message;
    }
    
    /**
     * 检查是否可以重试
     *
     * @param message 消息
     * @return 是否可以重试
     */
    public static boolean canRetry(MqMessage message) {
        if (message == null || message.getMaxRetryCount() == null) {
            return false;
        }
        
        int currentRetryCount = message.getRetryCount() != null ? message.getRetryCount() : 0;
        return currentRetryCount < message.getMaxRetryCount();
    }
}
