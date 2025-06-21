package io.github.kk01001.dynamic.mq.core;

import io.github.kk01001.dynamic.mq.model.MqMessage;

/**
 * @author linshiqiang
 * @date 2025-06-21 21:41:29
 * @description 高级MQ生产者接口，定义所有MQ都应该支持的高级功能
 */
public interface AdvancedMqProducer extends MqProducer {
    
    /**
     * 检查是否支持异步发送
     *
     * @return 是否支持
     */
    default boolean supportsAsync() {
        return true;
    }
    
    /**
     * 检查是否支持OneWay发送
     *
     * @return 是否支持
     */
    default boolean supportsOneWay() {
        return true;
    }
    
    /**
     * 检查是否支持顺序消息
     *
     * @return 是否支持
     */
    default boolean supportsOrderly() {
        return false;
    }
    
    /**
     * 检查是否支持事务消息
     *
     * @return 是否支持
     */
    default boolean supportsTransaction() {
        return false;
    }
    
    /**
     * 检查是否支持延迟消息
     *
     * @return 是否支持
     */
    default boolean supportsDelay() {
        return false;
    }
    
    /**
     * 获取支持的延迟级别（如果支持延迟消息）
     *
     * @return 延迟级别数组，单位毫秒
     */
    default long[] getSupportedDelayLevels() {
        return new long[0];
    }
    
    /**
     * 获取最大批量发送数量
     *
     * @return 最大批量数量
     */
    default int getMaxBatchSize() {
        return 100;
    }
    
    /**
     * 获取功能描述
     *
     * @return 功能描述
     */
    default String getFeatureDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("MQ类型: ").append(getProducerType()).append("\n");
        sb.append("异步发送: ").append(supportsAsync() ? "支持" : "不支持").append("\n");
        sb.append("OneWay发送: ").append(supportsOneWay() ? "支持" : "不支持").append("\n");
        sb.append("顺序消息: ").append(supportsOrderly() ? "支持" : "不支持").append("\n");
        sb.append("事务消息: ").append(supportsTransaction() ? "支持" : "不支持").append("\n");
        sb.append("延迟消息: ").append(supportsDelay() ? "支持" : "不支持").append("\n");
        sb.append("最大批量: ").append(getMaxBatchSize());
        return sb.toString();
    }
}
