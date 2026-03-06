package io.github.kk01001.redisson.circuitbreaker;

/**
 * @author kk01001
 * @date 2026-03-06 21:45:00
 * @description 熔断器状态变更监听器
 * <p>
 * 当熔断器状态发生变化时触发回调。
 * 典型用途：
 * <ul>
 *     <li>OPEN -> CLOSED：触发积压数据回放或全量同步</li>
 *     <li>CLOSED -> OPEN：发送告警通知</li>
 *     <li>记录状态变更日志</li>
 * </ul>
 * </p>
 */
@FunctionalInterface
public interface CircuitBreakerStateChangeListener {

    /**
     * 状态变更回调
     *
     * @param from 变更前状态
     * @param to   变更后状态
     */
    void onStateChange(CircuitBreakerState from, CircuitBreakerState to);
}
