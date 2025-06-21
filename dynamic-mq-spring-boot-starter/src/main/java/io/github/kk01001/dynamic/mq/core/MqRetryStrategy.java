package io.github.kk01001.dynamic.mq.core;

import io.github.kk01001.dynamic.mq.model.MqMessage;

/**
 * @author linshiqiang
 * @date 2025-06-21 21:41:29
 * @description MQ重试策略接口
 */
public interface MqRetryStrategy {
    
    /**
     * 是否应该重试
     *
     * @param message 消息对象
     * @param currentAttempt 当前重试次数
     * @param exception 异常信息
     * @return 是否重试
     */
    boolean shouldRetry(MqMessage message, int currentAttempt, Exception exception);
    
    /**
     * 获取重试延迟时间
     *
     * @param currentAttempt 当前重试次数
     * @return 延迟时间（毫秒）
     */
    long getRetryDelay(int currentAttempt);
    
    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    int getMaxRetryTimes();
    
    /**
     * 创建固定延迟重试策略
     *
     * @param maxRetryTimes 最大重试次数
     * @param fixedDelay 固定延迟时间（毫秒）
     * @return 重试策略
     */
    static MqRetryStrategy fixedDelay(int maxRetryTimes, long fixedDelay) {
        return new FixedDelayRetryStrategy(maxRetryTimes, fixedDelay);
    }
    
    /**
     * 创建指数退避重试策略
     *
     * @param maxRetryTimes 最大重试次数
     * @param initialDelay 初始延迟时间（毫秒）
     * @param multiplier 倍数
     * @return 重试策略
     */
    static MqRetryStrategy exponentialBackoff(int maxRetryTimes, long initialDelay, double multiplier) {
        return new ExponentialBackoffRetryStrategy(maxRetryTimes, initialDelay, multiplier);
    }
    
    /**
     * 创建自定义重试策略
     *
     * @param maxRetryTimes 最大重试次数
     * @param delayCalculator 延迟计算器
     * @return 重试策略
     */
    static MqRetryStrategy custom(int maxRetryTimes, java.util.function.IntToLongFunction delayCalculator) {
        return new CustomRetryStrategy(maxRetryTimes, delayCalculator);
    }
    
    /**
     * 固定延迟重试策略
     */
    class FixedDelayRetryStrategy implements MqRetryStrategy {
        private final int maxRetryTimes;
        private final long fixedDelay;
        
        public FixedDelayRetryStrategy(int maxRetryTimes, long fixedDelay) {
            this.maxRetryTimes = maxRetryTimes;
            this.fixedDelay = fixedDelay;
        }
        
        @Override
        public boolean shouldRetry(MqMessage message, int currentAttempt, Exception exception) {
            return currentAttempt < maxRetryTimes;
        }
        
        @Override
        public long getRetryDelay(int currentAttempt) {
            return fixedDelay;
        }
        
        @Override
        public int getMaxRetryTimes() {
            return maxRetryTimes;
        }
    }
    
    /**
     * 指数退避重试策略
     */
    class ExponentialBackoffRetryStrategy implements MqRetryStrategy {
        private final int maxRetryTimes;
        private final long initialDelay;
        private final double multiplier;
        
        public ExponentialBackoffRetryStrategy(int maxRetryTimes, long initialDelay, double multiplier) {
            this.maxRetryTimes = maxRetryTimes;
            this.initialDelay = initialDelay;
            this.multiplier = multiplier;
        }
        
        @Override
        public boolean shouldRetry(MqMessage message, int currentAttempt, Exception exception) {
            return currentAttempt < maxRetryTimes;
        }
        
        @Override
        public long getRetryDelay(int currentAttempt) {
            return (long) (initialDelay * Math.pow(multiplier, currentAttempt));
        }
        
        @Override
        public int getMaxRetryTimes() {
            return maxRetryTimes;
        }
    }
    
    /**
     * 自定义重试策略
     */
    class CustomRetryStrategy implements MqRetryStrategy {
        private final int maxRetryTimes;
        private final java.util.function.IntToLongFunction delayCalculator;
        
        public CustomRetryStrategy(int maxRetryTimes, java.util.function.IntToLongFunction delayCalculator) {
            this.maxRetryTimes = maxRetryTimes;
            this.delayCalculator = delayCalculator;
        }
        
        @Override
        public boolean shouldRetry(MqMessage message, int currentAttempt, Exception exception) {
            return currentAttempt < maxRetryTimes;
        }
        
        @Override
        public long getRetryDelay(int currentAttempt) {
            return delayCalculator.applyAsLong(currentAttempt);
        }
        
        @Override
        public int getMaxRetryTimes() {
            return maxRetryTimes;
        }
    }
}
