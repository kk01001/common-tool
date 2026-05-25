package io.github.archer099.redisson.retry;

/**
 * @author archer099
 * @date 2026-03-06 22:00:00
 * @description 重试队列溢出处理策略接口
 * <p>
 * 当重试队列已满且备份集群仍不可用时，通过此接口处理无法入队的操作。
 * </p>
 * <p>
 * 默认实现仅记录日志和指标。用户可自定义实现，例如：
 * <ul>
 *     <li>写入本地文件（WAL 日志），后续手动回放</li>
 *     <li>发送到 MQ 延迟队列</li>
 *     <li>写入数据库操作日志表</li>
 *     <li>触发告警通知运维人员</li>
 * </ul>
 * </p>
 */
public interface DualWriteOverflowHandler {

    /**
     * 处理队列溢出的任务
     *
     * @param task      无法入队的任务
     * @param reason    溢出原因
     * @param queueSize 当前队列大小
     */
    void onOverflow(RetryTask task, String reason, int queueSize);
}
