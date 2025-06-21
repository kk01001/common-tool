package io.github.kk01001.dynamic.mq.core;

import io.github.kk01001.dynamic.mq.model.MqMessage;

/**
 * @author linshiqiang
 * @date 2025-06-21 21:41:29
 * @description MQ死信队列处理器接口
 */
public interface MqDeadLetterHandler {
    
    /**
     * 处理死信消息
     *
     * @param message 死信消息
     * @param originalTopic 原始主题
     * @param consumerGroup 消费者组
     * @param retryTimes 重试次数
     * @param lastException 最后一次异常
     */
    void handleDeadLetter(MqMessage message, String originalTopic, String consumerGroup, 
                         int retryTimes, Exception lastException);
    
    /**
     * 获取死信队列主题名称
     *
     * @param originalTopic 原始主题
     * @param consumerGroup 消费者组
     * @return 死信队列主题
     */
    default String getDeadLetterTopic(String originalTopic, String consumerGroup) {
        return String.format("%s_DLQ_%s", originalTopic, consumerGroup);
    }
    
    /**
     * 是否启用死信队列
     *
     * @return 是否启用
     */
    default boolean isEnabled() {
        return true;
    }
    
    /**
     * 创建默认死信处理器
     *
     * @return 默认处理器
     */
    static MqDeadLetterHandler defaultHandler() {
        return new DefaultDeadLetterHandler();
    }
    
    /**
     * 创建日志记录死信处理器
     *
     * @return 日志处理器
     */
    static MqDeadLetterHandler loggingHandler() {
        return new LoggingDeadLetterHandler();
    }
    
    /**
     * 创建自定义死信处理器
     *
     * @param handler 自定义处理逻辑
     * @return 自定义处理器
     */
    static MqDeadLetterHandler custom(DeadLetterProcessor handler) {
        return new CustomDeadLetterHandler(handler);
    }
    
    /**
     * 死信处理器函数接口
     */
    @FunctionalInterface
    interface DeadLetterProcessor {
        void process(MqMessage message, String originalTopic, String consumerGroup, 
                    int retryTimes, Exception lastException);
    }
    
    /**
     * 默认死信处理器实现
     */
    class DefaultDeadLetterHandler implements MqDeadLetterHandler {
        
        @Override
        public void handleDeadLetter(MqMessage message, String originalTopic, String consumerGroup, 
                                   int retryTimes, Exception lastException) {
            // 默认实现：记录日志并存储到死信队列
            String deadLetterTopic = getDeadLetterTopic(originalTopic, consumerGroup);
            
            // 添加死信相关属性
            if (message.getProperties() == null) {
                message.setProperties(new java.util.HashMap<>());
            }
            
            message.getProperties().put("DEAD_LETTER_ORIGINAL_TOPIC", originalTopic);
            message.getProperties().put("DEAD_LETTER_CONSUMER_GROUP", consumerGroup);
            message.getProperties().put("DEAD_LETTER_RETRY_TIMES", retryTimes);
            message.getProperties().put("DEAD_LETTER_TIMESTAMP", System.currentTimeMillis());
            message.getProperties().put("DEAD_LETTER_EXCEPTION", lastException.getMessage());
            
            // 这里应该将消息发送到死信队列
            // 具体实现由各个MQ的实现类处理
            System.err.printf("消息进入死信队列: topic=%s, messageId=%s, retryTimes=%d%n", 
                    deadLetterTopic, message.getMessageId(), retryTimes);
        }
    }
    
    /**
     * 日志记录死信处理器
     */
    class LoggingDeadLetterHandler implements MqDeadLetterHandler {
        
        @Override
        public void handleDeadLetter(MqMessage message, String originalTopic, String consumerGroup, 
                                   int retryTimes, Exception lastException) {
            // 只记录日志，不做其他处理
            System.err.printf("死信消息: topic=%s, messageId=%s, group=%s, retryTimes=%d, error=%s%n",
                    originalTopic, message.getMessageId(), consumerGroup, retryTimes, 
                    lastException.getMessage());
        }
    }
    
    /**
     * 自定义死信处理器
     */
    class CustomDeadLetterHandler implements MqDeadLetterHandler {
        private final DeadLetterProcessor processor;
        
        public CustomDeadLetterHandler(DeadLetterProcessor processor) {
            this.processor = processor;
        }
        
        @Override
        public void handleDeadLetter(MqMessage message, String originalTopic, String consumerGroup, 
                                   int retryTimes, Exception lastException) {
            processor.process(message, originalTopic, consumerGroup, retryTimes, lastException);
        }
    }
}
