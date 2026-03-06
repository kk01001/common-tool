package io.github.kk01001.redisson.circuitbreaker;

import io.github.kk01001.redisson.properties.MultiRedissonProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author kk01001
 * @date 2026-01-15 18:00:00
 * @description 双写熔断器
 * <p>
 * 当备份集群写入失败率过高时自动熔断，防止影响主流程性能。
 * 熔断后会定期尝试恢复。
 * 配置直接从 MultiRedissonProperties.CircuitBreaker 读取，支持 Nacos 动态刷新。
 * </p>
 * <p>
 * 使用环形缓冲区实现基于计数的滑动窗口，保证窗口重置的原子性。
 * 支持状态变更监听器，可用于触发恢复后的数据同步。
 * </p>
 */
public class DualWriteCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(DualWriteCircuitBreaker.class);

    /**
     * 熔断器状态
     */
    private final AtomicReference<CircuitBreakerState> state = new AtomicReference<>(CircuitBreakerState.CLOSED);

    /**
     * 熔断打开时间
     */
    private final AtomicLong openTime = new AtomicLong(0);

    /**
     * 半开状态下的成功次数
     */
    private final AtomicInteger halfOpenSuccessCount = new AtomicInteger(0);

    /**
     * 滑动窗口（环形缓冲区）
     */
    private final SlidingWindow slidingWindow;

    /**
     * 配置（直接引用 Properties，支持动态刷新）
     */
    private final MultiRedissonProperties.CircuitBreaker config;

    /**
     * 状态变更监听器列表
     */
    private final List<CircuitBreakerStateChangeListener> listeners = new CopyOnWriteArrayList<>();

    public DualWriteCircuitBreaker(MultiRedissonProperties.CircuitBreaker config) {
        this.config = config;
        this.slidingWindow = new SlidingWindow(config.getSlidingWindowSize());
        log.info("DualWriteCircuitBreaker initialized with config: enabled={}, failureRateThreshold={}%, " +
                        "slidingWindowSize={}ms, minimumNumberOfCalls={}, waitDurationInOpenState={}ms, " +
                        "permittedCallsInHalfOpenState={}",
                config.isEnabled(), config.getFailureRateThreshold(), config.getSlidingWindowSize(),
                config.getMinimumNumberOfCalls(), config.getWaitDurationInOpenState(),
                config.getPermittedCallsInHalfOpenState());
    }

    /**
     * 获取配置
     */
    public MultiRedissonProperties.CircuitBreaker getConfig() {
        return config;
    }

    /**
     * 注册状态变更监听器
     */
    public void addStateChangeListener(CircuitBreakerStateChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除状态变更监听器
     */
    public void removeStateChangeListener(CircuitBreakerStateChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 判断是否允许执行
     */
    public boolean allowRequest() {
        if (!config.isEnabled()) {
            return true;
        }

        CircuitBreakerState currentState = state.get();

        switch (currentState) {
            case CLOSED:
                return true;

            case OPEN:
                long now = System.currentTimeMillis();
                if (now - openTime.get() >= config.getWaitDurationInOpenState()) {
                    if (state.compareAndSet(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN)) {
                        halfOpenSuccessCount.set(0);
                        log.info("CircuitBreaker state changed: OPEN -> HALF_OPEN");
                        notifyStateChange(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN);
                    }
                    return true;
                }
                return false;

            case HALF_OPEN:
                return halfOpenSuccessCount.get() < config.getPermittedCallsInHalfOpenState();

            default:
                return true;
        }
    }

    /**
     * 记录成功
     */
    public void recordSuccess() {
        if (!config.isEnabled()) {
            return;
        }

        CircuitBreakerState currentState = state.get();

        if (currentState == CircuitBreakerState.HALF_OPEN) {
            int count = halfOpenSuccessCount.incrementAndGet();
            if (count >= config.getPermittedCallsInHalfOpenState()) {
                if (state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.CLOSED)) {
                    slidingWindow.reset();
                    log.info("CircuitBreaker state changed: HALF_OPEN -> CLOSED (recovered)");
                    notifyStateChange(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.CLOSED);
                }
            }
        } else if (currentState == CircuitBreakerState.CLOSED) {
            slidingWindow.recordSuccess();
        }
    }

    /**
     * 记录失败
     */
    public void recordFailure() {
        if (!config.isEnabled()) {
            return;
        }

        CircuitBreakerState currentState = state.get();

        if (currentState == CircuitBreakerState.HALF_OPEN) {
            if (state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.OPEN)) {
                openTime.set(System.currentTimeMillis());
                log.warn("CircuitBreaker state changed: HALF_OPEN -> OPEN (failure in half-open)");
                notifyStateChange(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.OPEN);
            }
        } else if (currentState == CircuitBreakerState.CLOSED) {
            slidingWindow.recordFailure();
            checkFailureThreshold();
        }
    }

    /**
     * 检查失败率是否达到阈值
     */
    private void checkFailureThreshold() {
        SlidingWindow.Snapshot snapshot = slidingWindow.getSnapshot();

        if (snapshot.totalCount() < config.getMinimumNumberOfCalls()) {
            return;
        }

        if (snapshot.failureRate() >= config.getFailureRateThreshold()) {
            if (state.compareAndSet(CircuitBreakerState.CLOSED, CircuitBreakerState.OPEN)) {
                openTime.set(System.currentTimeMillis());
                log.warn("CircuitBreaker state changed: CLOSED -> OPEN, failureRate: {}%, threshold: {}%",
                        String.format("%.2f", snapshot.failureRate()), config.getFailureRateThreshold());
                notifyStateChange(CircuitBreakerState.CLOSED, CircuitBreakerState.OPEN);
            }
        }
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        slidingWindow.reset();
        halfOpenSuccessCount.set(0);
    }

    /**
     * 强制打开熔断器
     */
    public void forceOpen() {
        CircuitBreakerState prev = state.getAndSet(CircuitBreakerState.OPEN);
        openTime.set(System.currentTimeMillis());
        log.warn("CircuitBreaker force opened");
        if (prev != CircuitBreakerState.OPEN) {
            notifyStateChange(prev, CircuitBreakerState.OPEN);
        }
    }

    /**
     * 强制关闭熔断器
     */
    public void forceClose() {
        CircuitBreakerState prev = state.getAndSet(CircuitBreakerState.CLOSED);
        reset();
        log.info("CircuitBreaker force closed");
        if (prev != CircuitBreakerState.CLOSED) {
            notifyStateChange(prev, CircuitBreakerState.CLOSED);
        }
    }

    /**
     * 获取当前状态
     */
    public CircuitBreakerState getState() {
        return state.get();
    }

    /**
     * 获取统计信息
     */
    public CircuitBreakerStats getStats() {
        SlidingWindow.Snapshot snapshot = slidingWindow.getSnapshot();

        return new CircuitBreakerStats(
                state.get(),
                snapshot.totalCount(),
                snapshot.successCount(),
                snapshot.failureCount(),
                snapshot.failureRate(),
                openTime.get()
        );
    }

    /**
     * 通知状态变更
     */
    private void notifyStateChange(CircuitBreakerState from, CircuitBreakerState to) {
        for (CircuitBreakerStateChangeListener listener : listeners) {
            try {
                listener.onStateChange(from, to);
            } catch (Exception e) {
                log.error("CircuitBreaker state change listener error, from={}, to={}", from, to, e);
            }
        }
    }

    /**
     * 熔断器统计信息
     */
    public record CircuitBreakerStats(
            CircuitBreakerState state,
            int totalCalls,
            int successCalls,
            int failureCalls,
            double failureRate,
            long openTimestamp
    ) {
    }

    /**
     * 基于时间的滑动窗口，使用锁保证重置原子性
     */
    static class SlidingWindow {

        private final long windowSizeMs;
        private final ReentrantLock lock = new ReentrantLock();

        private int failureCount;
        private int successCount;
        private int totalCount;
        private long windowStartTime;

        SlidingWindow(long windowSizeMs) {
            this.windowSizeMs = windowSizeMs;
            this.windowStartTime = System.currentTimeMillis();
        }

        void recordSuccess() {
            lock.lock();
            try {
                rollWindowIfNeeded();
                successCount++;
                totalCount++;
            } finally {
                lock.unlock();
            }
        }

        void recordFailure() {
            lock.lock();
            try {
                rollWindowIfNeeded();
                failureCount++;
                totalCount++;
            } finally {
                lock.unlock();
            }
        }

        Snapshot getSnapshot() {
            lock.lock();
            try {
                rollWindowIfNeeded();
                double rate = totalCount > 0 ? (double) failureCount / totalCount * 100 : 0;
                return new Snapshot(totalCount, successCount, failureCount, rate);
            } finally {
                lock.unlock();
            }
        }

        void reset() {
            lock.lock();
            try {
                failureCount = 0;
                successCount = 0;
                totalCount = 0;
                windowStartTime = System.currentTimeMillis();
            } finally {
                lock.unlock();
            }
        }

        /**
         * 如果当前时间超出窗口范围，重置计数器（原子操作，在锁内调用）
         */
        private void rollWindowIfNeeded() {
            long now = System.currentTimeMillis();
            if (now - windowStartTime >= windowSizeMs) {
                failureCount = 0;
                successCount = 0;
                totalCount = 0;
                windowStartTime = now;
            }
        }

        record Snapshot(int totalCount, int successCount, int failureCount, double failureRate) {
        }
    }
}
