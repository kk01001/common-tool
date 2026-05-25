package io.github.archer099.redisson.circuitbreaker;

/**
 * @author archer099
 * @date 2026-01-15 18:00:00
 * @description 熔断器状态枚举
 */
public enum CircuitBreakerState {

    /**
     * 关闭状态（正常工作）
     */
    CLOSED,

    /**
     * 打开状态（熔断中，拒绝请求）
     */
    OPEN,

    /**
     * 半开状态（尝试恢复）
     */
    HALF_OPEN
}
