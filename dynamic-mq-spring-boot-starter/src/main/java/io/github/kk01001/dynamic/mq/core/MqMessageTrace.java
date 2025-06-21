package io.github.kk01001.dynamic.mq.core;

import io.github.kk01001.dynamic.mq.model.MqMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2025-06-21 21:41:29
 * @description MQ消息轨迹接口和实现
 */
public interface MqMessageTrace {
    
    /**
     * 记录消息发送轨迹
     *
     * @param message 消息对象
     * @param result 发送结果
     */
    void traceSend(MqMessage message, boolean result);
    
    /**
     * 记录消息消费轨迹
     *
     * @param message 消息对象
     * @param consumerGroup 消费者组
     * @param result 消费结果
     * @param costTime 消费耗时
     */
    void traceConsume(MqMessage message, String consumerGroup, boolean result, long costTime);
    
    /**
     * 查询消息轨迹
     *
     * @param messageId 消息ID
     * @return 轨迹信息列表
     */
    List<TraceInfo> queryTrace(String messageId);
    
    /**
     * 是否启用消息轨迹
     *
     * @return 是否启用
     */
    default boolean isEnabled() {
        return true;
    }
    
    /**
     * 轨迹信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class TraceInfo {
        /**
         * 消息ID
         */
        private String messageId;
        
        /**
         * 轨迹类型
         */
        private TraceType traceType;
        
        /**
         * 主题
         */
        private String topic;
        
        /**
         * 标签
         */
        private String tag;
        
        /**
         * 消费者组（消费轨迹）
         */
        private String consumerGroup;
        
        /**
         * 客户端IP
         */
        private String clientIp;
        
        /**
         * 时间戳
         */
        private Long timestamp;
        
        /**
         * 结果状态
         */
        private boolean success;
        
        /**
         * 耗时（毫秒）
         */
        private Long costTime;
        
        /**
         * 异常信息
         */
        private String exception;
        
        /**
         * 扩展属性
         */
        private Map<String, Object> properties;
    }
    
    /**
     * 轨迹类型枚举
     */
    enum TraceType {
        /**
         * 发送轨迹
         */
        SEND,
        
        /**
         * 消费轨迹
         */
        CONSUME
    }
    
    /**
     * 创建默认轨迹记录器
     *
     * @return 默认记录器
     */
    static MqMessageTrace defaultTrace() {
        return new DefaultMessageTrace();
    }
    
    /**
     * 创建日志轨迹记录器
     *
     * @return 日志记录器
     */
    static MqMessageTrace loggingTrace() {
        return new LoggingMessageTrace();
    }
    
    /**
     * 创建自定义轨迹记录器
     *
     * @param tracer 自定义轨迹处理器
     * @return 自定义记录器
     */
    static MqMessageTrace custom(MessageTracer tracer) {
        return new CustomMessageTrace(tracer);
    }
    
    /**
     * 消息轨迹处理器接口
     */
    interface MessageTracer {
        void trace(TraceInfo traceInfo);
        List<TraceInfo> query(String messageId);
    }
    
    /**
     * 默认消息轨迹实现
     */
    class DefaultMessageTrace implements MqMessageTrace {
        private final Map<String, List<TraceInfo>> traceStore = new java.util.concurrent.ConcurrentHashMap<>();
        
        @Override
        public void traceSend(MqMessage message, boolean result) {
            TraceInfo traceInfo = TraceInfo.builder()
                    .messageId(message.getMessageId())
                    .traceType(TraceType.SEND)
                    .topic(message.getTopic())
                    .tag(message.getTag())
                    .clientIp(getLocalIp())
                    .timestamp(System.currentTimeMillis())
                    .success(result)
                    .build();
            
            storeTrace(traceInfo);
        }
        
        @Override
        public void traceConsume(MqMessage message, String consumerGroup, boolean result, long costTime) {
            TraceInfo traceInfo = TraceInfo.builder()
                    .messageId(message.getMessageId())
                    .traceType(TraceType.CONSUME)
                    .topic(message.getTopic())
                    .tag(message.getTag())
                    .consumerGroup(consumerGroup)
                    .clientIp(getLocalIp())
                    .timestamp(System.currentTimeMillis())
                    .success(result)
                    .costTime(costTime)
                    .build();
            
            storeTrace(traceInfo);
        }
        
        @Override
        public List<TraceInfo> queryTrace(String messageId) {
            return traceStore.getOrDefault(messageId, java.util.Collections.emptyList());
        }
        
        private void storeTrace(TraceInfo traceInfo) {
            traceStore.computeIfAbsent(traceInfo.getMessageId(), k -> new java.util.ArrayList<>())
                    .add(traceInfo);
        }
        
        private String getLocalIp() {
            try {
                return java.net.InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                return "unknown";
            }
        }
    }
    
    /**
     * 日志消息轨迹实现
     */
    class LoggingMessageTrace implements MqMessageTrace {
        
        @Override
        public void traceSend(MqMessage message, boolean result) {
            System.out.printf("TRACE_SEND: messageId=%s, topic=%s, result=%s, timestamp=%d%n",
                    message.getMessageId(), message.getTopic(), result, System.currentTimeMillis());
        }
        
        @Override
        public void traceConsume(MqMessage message, String consumerGroup, boolean result, long costTime) {
            System.out.printf("TRACE_CONSUME: messageId=%s, topic=%s, group=%s, result=%s, costTime=%d, timestamp=%d%n",
                    message.getMessageId(), message.getTopic(), consumerGroup, result, costTime, System.currentTimeMillis());
        }
        
        @Override
        public List<TraceInfo> queryTrace(String messageId) {
            // 日志模式不支持查询
            return java.util.Collections.emptyList();
        }
    }
    
    /**
     * 自定义消息轨迹实现
     */
    class CustomMessageTrace implements MqMessageTrace {
        private final MessageTracer tracer;
        
        public CustomMessageTrace(MessageTracer tracer) {
            this.tracer = tracer;
        }
        
        @Override
        public void traceSend(MqMessage message, boolean result) {
            TraceInfo traceInfo = TraceInfo.builder()
                    .messageId(message.getMessageId())
                    .traceType(TraceType.SEND)
                    .topic(message.getTopic())
                    .tag(message.getTag())
                    .timestamp(System.currentTimeMillis())
                    .success(result)
                    .build();
            
            tracer.trace(traceInfo);
        }
        
        @Override
        public void traceConsume(MqMessage message, String consumerGroup, boolean result, long costTime) {
            TraceInfo traceInfo = TraceInfo.builder()
                    .messageId(message.getMessageId())
                    .traceType(TraceType.CONSUME)
                    .topic(message.getTopic())
                    .tag(message.getTag())
                    .consumerGroup(consumerGroup)
                    .timestamp(System.currentTimeMillis())
                    .success(result)
                    .costTime(costTime)
                    .build();
            
            tracer.trace(traceInfo);
        }
        
        @Override
        public List<TraceInfo> queryTrace(String messageId) {
            return tracer.query(messageId);
        }
    }
}
