package io.github.kk01001.redisson.circuitbreaker;

import io.github.kk01001.redisson.properties.MultiRedissonProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author kk01001
 * @date 2026-01-15 18:00:00
 * @description 双写熔断器
 * <p>
 * 当备份集群写入失败率过高时自动熔断，防止影响主流程性能。
 * 熔断后会定期尝试恢复。
 * 配置直接从 MultiRedissonProperties.CircuitBreaker 读取，支持 Nacos 动态刷新。
 * </p>
 */
public class DualWriteCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(DualWriteCircuitBreaker.class);

    /**
     * 熔断器状态
     */
    private final AtomicReference<CircuitBreakerState> state = new AtomicReference<>(CircuitBreakerState.CLOSED);

    /**
     * 滑动窗口内的失败次数
     */
    private final AtomicInteger failureCount = new AtomicInteger(0);

    /**
     * 滑动窗口内的成功次数
     */
    private final AtomicInteger successCount = new AtomicInteger(0);

    /**
     * 滑动窗口内的总请求数
     */
    private final AtomicInteger totalCount = new AtomicInteger(0);

    /**
     * 熔断打开时间
     */
    private final AtomicLong openTime = new AtomicLong(0);

    /**
     * 半开状态下的成功次数
     */
    private final AtomicInteger halfOpenSuccessCount = new AtomicInteger(0);

    /**
     * 上次重置窗口的时间
     */
    private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());

    /**
     * 配置（直接引用 Properties，支持动态刷新）
     */
    private final MultiRedissonProperties.CircuitBreaker config;

    public DualWriteCircuitBreaker(MultiRedissonProperties.CircuitBreaker config) {
        this.config = config;
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
                // 检查是否到达恢复时间
                long now = System.currentTimeMillis();
                if (now - openTime.get() >= config.getWaitDurationInOpenState()) {
                    // 尝试进入半开状态
                    if (state.compareAndSet(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN)) {
                        halfOpenSuccessCount.set(0);
                        log.info("CircuitBreaker state changed: OPEN -> HALF_OPEN");
                    }
                    return true;
                }
                return false;

            case HALF_OPEN:
                // 半开状态允许有限的请求通过
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

        checkAndResetWindow();

        CircuitBreakerState currentState = state.get();

        if (currentState == CircuitBreakerState.HALF_OPEN) {
            int count = halfOpenSuccessCount.incrementAndGet();
            if (count >= config.getPermittedCallsInHalfOpenState()) {
                // 半开状态下连续成功次数达到阈值，关闭熔断器
                if (state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.CLOSED)) {
                    reset();
                    log.info("CircuitBreaker state changed: HALF_OPEN -> CLOSED (recovered)");
                }
            }
        } else if (currentState == CircuitBreakerState.CLOSED) {
            successCount.incrementAndGet();
            totalCount.incrementAndGet();
        }
    }

    /**
     * 记录失败
     */
    public void recordFailure() {
        if (!config.isEnabled()) {
            return;
        }

        checkAndResetWindow();

        CircuitBreakerState currentState = state.get();

        if (currentState == CircuitBreakerState.HALF_OPEN) {
            // 半开状态下失败，重新打开熔断器
            if (state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.OPEN)) {
                openTime.set(System.currentTimeMillis());
                log.warn("CircuitBreaker state changed: HALF_OPEN -> OPEN (failure in half-open)");
            }
        } else if (currentState == CircuitBreakerState.CLOSED) {
            failureCount.incrementAndGet();
            totalCount.incrementAndGet();

            // 检查是否需要打开熔断器
            checkFailureThreshold();
        }
    }

    /**
     * 检查失败率是否达到阈值
     */
    private void checkFailureThreshold() {
        int total = totalCount.get();
        int failures = failureCount.get();

        // 需要达到最小请求数才进行熔断判断
        if (total < config.getMinimumNumberOfCalls()) {
            return;
        }

        double failureRate = (double) failures / total * 100;

        if (failureRate >= config.getFailureRateThreshold()) {
            if (state.compareAndSet(CircuitBreakerState.CLOSED, CircuitBreakerState.OPEN)) {
                openTime.set(System.currentTimeMillis());
                log.warn("CircuitBreaker state changed: CLOSED -> OPEN, failureRate: {}%, threshold: {}%",
                        String.format("%.2f", failureRate), config.getFailureRateThreshold());
            }
        }
    }

    /**
     * 检查并重置滑动窗口
     */
    private void checkAndResetWindow() {
        long now = System.currentTimeMillis();
        long lastReset = lastResetTime.get();

        if (now - lastReset >= config.getSlidingWindowSize()) {
            if (lastResetTime.compareAndSet(lastReset, now)) {
                // 重置窗口计数（但不重置状态）
                failureCount.set(0);
                successCount.set(0);
                totalCount.set(0);
            }
        }
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        failureCount.set(0);
        successCount.set(0);
        totalCount.set(0);
        halfOpenSuccessCount.set(0);
        lastResetTime.set(System.currentTimeMillis());
    }

    /**
     * 强制打开熔断器
     */
    public void forceOpen() {
        state.set(CircuitBreakerState.OPEN);
        openTime.set(System.currentTimeMillis());
        log.warn("CircuitBreaker force opened");
    }

    /**
     * 强制关闭熔断器
     */
    public void forceClose() {
        state.set(CircuitBreakerState.CLOSED);
        reset();
        log.info("CircuitBreaker force closed");
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
        int total = totalCount.get();
        int failures = failureCount.get();
        int successes = successCount.get();
        double failureRate = total > 0 ? (double) failures / total * 100 : 0;

        return new CircuitBreakerStats(
                state.get(),
                total,
                successes,
                failures,
                failureRate,
                openTime.get()
        );
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
}
