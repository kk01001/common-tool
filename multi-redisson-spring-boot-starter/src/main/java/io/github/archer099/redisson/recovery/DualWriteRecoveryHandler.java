package io.github.archer099.redisson.recovery;

import io.github.archer099.redisson.circuitbreaker.CircuitBreakerState;

/**
 * @author archer099
 * @date 2026-03-06 21:50:00
 * @description 双写恢复处理器接口
 * <p>
 * 当熔断器从打开状态恢复到关闭状态时触发，用于执行数据同步或补偿操作。
 * </p>
 * <p>
 * 用户可自定义实现，例如：
 * <ul>
 *     <li>触发主备数据全量对比和修复</li>
 *     <li>回放操作日志</li>
 *     <li>发送恢复通知</li>
 *     <li>记录恢复事件到审计日志</li>
 * </ul>
 * </p>
 */
public interface DualWriteRecoveryHandler {

    /**
     * 熔断器恢复时触发
     *
     * @param context 恢复上下文
     */
    void onRecovery(RecoveryContext context);

    /**
     * 恢复上下文
     *
     * @param previousState    恢复前的状态
     * @param openTimestamp    熔断打开的时间戳
     * @param recoveryTimestamp 恢复的时间戳
     * @param durationMs       熔断持续时间（毫秒）
     */
    record RecoveryContext(
            CircuitBreakerState previousState,
            long openTimestamp,
            long recoveryTimestamp,
            long durationMs
    ) {
    }
}
