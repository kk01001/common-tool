package io.github.kk01001.redisson.retry;

/**
 * @author kk01001
 * @date 2026-03-06 21:32:00
 * @description 双写失败处理策略接口
 * <p>
 * 当备份集群写入失败、熔断器打开跳过写入、或线程池拒绝任务时，
 * 通过此接口处理失败的操作。
 * </p>
 * <p>
 * 默认提供内存队列重试实现 {@link DefaultDualWriteFailureHandler}，
 * 用户可自定义实现，例如：
 * <ul>
 *     <li>基于 RocketMQ/Kafka 的持久化重试</li>
 *     <li>基于数据库的操作日志记录</li>
 *     <li>基于 Redis Stream 的重试队列</li>
 * </ul>
 * </p>
 */
public interface DualWriteFailureHandler {

    /**
     * 处理写入失败的操作
     *
     * @param task 失败的重试任务
     */
    void onWriteFailure(RetryTask task);

    /**
     * 处理熔断跳过的操作
     *
     * @param task 被熔断跳过的重试任务
     */
    void onCircuitBreakerSkip(RetryTask task);

    /**
     * 处理线程池拒绝的操作
     *
     * @param task 被线程池拒绝的重试任务
     */
    void onExecutorRejected(RetryTask task);

    /**
     * 处理超过最大重试次数的操作（死信）
     *
     * @param task 超过最大重试次数的任务
     */
    void onMaxRetryExceeded(RetryTask task);

    /**
     * 启动失败处理器（由框架在初始化时调用）
     */
    default void start() {
    }

    /**
     * 关闭失败处理器（由框架在销毁时调用）
     */
    default void shutdown() {
    }
}
