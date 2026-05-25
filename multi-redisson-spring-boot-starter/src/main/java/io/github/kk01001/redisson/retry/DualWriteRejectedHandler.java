package io.github.archer099.redisson.retry;

import io.github.archer099.redisson.monitor.DualWriteMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author archer099
 * @date 2026-03-06 21:32:00
 * @description 双写线程池拒绝策略
 * <p>
 * 线程池满时不阻塞调用者线程，而是将任务交给 {@link DualWriteFailureHandler} 处理。
 * 替代 {@link ThreadPoolExecutor.CallerRunsPolicy}，避免备份集群故障时阻塞业务线程。
 * </p>
 */
public class DualWriteRejectedHandler implements RejectedExecutionHandler {

    private static final Logger log = LoggerFactory.getLogger(DualWriteRejectedHandler.class);

    /**
     * 监控指标
     */
    private final DualWriteMetrics metrics;

    /**
     * 失败处理器
     */
    private final DualWriteFailureHandler failureHandler;

    public DualWriteRejectedHandler(DualWriteMetrics metrics, DualWriteFailureHandler failureHandler) {
        this.metrics = metrics;
        this.failureHandler = failureHandler;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (metrics != null) {
            metrics.recordSubmitFailure();
        }

        if (r instanceof RetryableRunnable retryable) {
            RetryTask task = new RetryTask(retryable.getAction(), retryable.getActionName());
            if (failureHandler != null) {
                failureHandler.onExecutorRejected(task);
            } else {
                log.warn("Dual write task rejected and no failure handler configured, task discarded: {}",
                        retryable.getActionName());
            }
        } else {
            log.warn("Dual write task rejected, executor queue full. Task type: {}", r.getClass().getSimpleName());
        }
    }
}
