package io.github.kk01001.redisson.monitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author kk01001
 * @date 2026-01-15 16:00:00
 * @description 双写监控指标
 */
public class DualWriteMetrics {

    /**
     * 总成功次数
     */
    private final LongAdder totalSuccess = new LongAdder();

    /**
     * 总失败次数
     */
    private final LongAdder totalFailure = new LongAdder();

    /**
     * 总提交任务次数
     */
    private final LongAdder totalSubmitted = new LongAdder();

    /**
     * 提交失败次数（线程池拒绝）
     */
    private final LongAdder submitFailure = new LongAdder();

    /**
     * 各操作成功次数
     */
    private final Map<String, LongAdder> operationSuccess = new ConcurrentHashMap<>();

    /**
     * 各操作失败次数
     */
    private final Map<String, LongAdder> operationFailure = new ConcurrentHashMap<>();

    /**
     * 线程池引用
     */
    private ThreadPoolExecutor executor;

    /**
     * 记录成功
     */
    public void recordSuccess(String operation) {
        totalSuccess.increment();
        operationSuccess.computeIfAbsent(operation, k -> new LongAdder()).increment();
    }

    /**
     * 记录失败
     */
    public void recordFailure(String operation) {
        totalFailure.increment();
        operationFailure.computeIfAbsent(operation, k -> new LongAdder()).increment();
    }

    /**
     * 记录任务提交
     */
    public void recordSubmit() {
        totalSubmitted.increment();
    }

    /**
     * 记录提交失败
     */
    public void recordSubmitFailure() {
        submitFailure.increment();
    }

    /**
     * 获取总成功次数
     */
    public long getTotalSuccess() {
        return totalSuccess.sum();
    }

    /**
     * 获取总失败次数
     */
    public long getTotalFailure() {
        return totalFailure.sum();
    }

    /**
     * 获取总提交次数
     */
    public long getTotalSubmitted() {
        return totalSubmitted.sum();
    }

    /**
     * 获取提交失败次数
     */
    public long getSubmitFailure() {
        return submitFailure.sum();
    }

    /**
     * 获取总执行次数
     */
    public long getTotalExecuted() {
        return getTotalSuccess() + getTotalFailure();
    }

    /**
     * 获取失败比例
     */
    public double getFailureRate() {
        long total = getTotalExecuted();
        if (total == 0) {
            return 0.0;
        }
        return (double) getTotalFailure() / total * 100;
    }

    /**
     * 获取成功比例
     */
    public double getSuccessRate() {
        long total = getTotalExecuted();
        if (total == 0) {
            return 100.0;
        }
        return (double) getTotalSuccess() / total * 100;
    }

    /**
     * 获取各操作成功次数
     */
    public Map<String, Long> getOperationSuccessCount() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        operationSuccess.forEach((k, v) -> result.put(k, v.sum()));
        return result;
    }

    /**
     * 获取各操作失败次数
     */
    public Map<String, Long> getOperationFailureCount() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        operationFailure.forEach((k, v) -> result.put(k, v.sum()));
        return result;
    }

    /**
     * 设置线程池
     */
    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    /**
     * 获取线程池状态
     */
    public ThreadPoolStatus getThreadPoolStatus() {
        if (executor == null) {
            return null;
        }
        return new ThreadPoolStatus(
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getPoolSize(),
                executor.getActiveCount(),
                executor.getQueue().size(),
                executor.getQueue().remainingCapacity(),
                executor.getCompletedTaskCount(),
                executor.getTaskCount(),
                executor.getLargestPoolSize()
        );
    }

    /**
     * 重置统计
     */
    public void reset() {
        totalSuccess.reset();
        totalFailure.reset();
        totalSubmitted.reset();
        submitFailure.reset();
        operationSuccess.clear();
        operationFailure.clear();
    }

    /**
     * 线程池状态
     */
    public record ThreadPoolStatus(
            int corePoolSize,
            int maxPoolSize,
            int currentPoolSize,
            int activeCount,
            int queueSize,
            int queueRemainingCapacity,
            long completedTaskCount,
            long totalTaskCount,
            int largestPoolSize
    ) {
        /**
         * 获取线程池使用率
         */
        public double getPoolUsageRate() {
            if (maxPoolSize == 0) {
                return 0.0;
            }
            return (double) activeCount / maxPoolSize * 100;
        }

        /**
         * 获取队列使用率
         */
        public double getQueueUsageRate() {
            int totalCapacity = queueSize + queueRemainingCapacity;
            if (totalCapacity == 0) {
                return 0.0;
            }
            return (double) queueSize / totalCapacity * 100;
        }
    }
}
