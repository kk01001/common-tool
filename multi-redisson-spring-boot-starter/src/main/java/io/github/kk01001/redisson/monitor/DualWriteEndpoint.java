package io.github.kk01001.redisson.monitor;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2026-01-15 16:00:00
 * @description 双写监控 Actuator 端点
 * <p>
 * 访问路径：/actuator/dualwrite
 * </p>
 */
@Endpoint(id = "dualwrite")
public class DualWriteEndpoint {

    private final DualWriteMetrics metrics;

    public DualWriteEndpoint(DualWriteMetrics metrics) {
        this.metrics = metrics;
    }

    /**
     * 获取所有监控信息
     * GET /actuator/dualwrite
     */
    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 基本信息
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 统计概览
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalSubmitted", metrics.getTotalSubmitted());
        overview.put("totalExecuted", metrics.getTotalExecuted());
        overview.put("totalSuccess", metrics.getTotalSuccess());
        overview.put("totalFailure", metrics.getTotalFailure());
        overview.put("submitFailure", metrics.getSubmitFailure());
        overview.put("successRate", String.format("%.2f%%", metrics.getSuccessRate()));
        overview.put("failureRate", String.format("%.2f%%", metrics.getFailureRate()));
        result.put("overview", overview);

        // 线程池状态
        DualWriteMetrics.ThreadPoolStatus poolStatus = metrics.getThreadPoolStatus();
        if (poolStatus != null) {
            Map<String, Object> threadPool = new LinkedHashMap<>();
            threadPool.put("corePoolSize", poolStatus.corePoolSize());
            threadPool.put("maxPoolSize", poolStatus.maxPoolSize());
            threadPool.put("currentPoolSize", poolStatus.currentPoolSize());
            threadPool.put("activeCount", poolStatus.activeCount());
            threadPool.put("largestPoolSize", poolStatus.largestPoolSize());
            threadPool.put("completedTaskCount", poolStatus.completedTaskCount());
            threadPool.put("totalTaskCount", poolStatus.totalTaskCount());
            threadPool.put("poolUsageRate", String.format("%.2f%%", poolStatus.getPoolUsageRate()));

            Map<String, Object> queue = new LinkedHashMap<>();
            queue.put("size", poolStatus.queueSize());
            queue.put("remainingCapacity", poolStatus.queueRemainingCapacity());
            queue.put("usageRate", String.format("%.2f%%", poolStatus.getQueueUsageRate()));
            threadPool.put("queue", queue);

            result.put("threadPool", threadPool);
        }

        // 各操作统计
        Map<String, Object> operations = new LinkedHashMap<>();
        operations.put("success", metrics.getOperationSuccessCount());
        operations.put("failure", metrics.getOperationFailureCount());
        result.put("operations", operations);

        return result;
    }

    /**
     * 获取指定类型的监控信息
     * GET /actuator/dualwrite/{type}
     *
     * @param type overview | threadPool | operations
     */
    @ReadOperation
    public Map<String, Object> infoByType(@Selector String type) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        switch (type) {
            case "overview" -> {
                result.put("totalSubmitted", metrics.getTotalSubmitted());
                result.put("totalExecuted", metrics.getTotalExecuted());
                result.put("totalSuccess", metrics.getTotalSuccess());
                result.put("totalFailure", metrics.getTotalFailure());
                result.put("submitFailure", metrics.getSubmitFailure());
                result.put("successRate", String.format("%.2f%%", metrics.getSuccessRate()));
                result.put("failureRate", String.format("%.2f%%", metrics.getFailureRate()));
            }
            case "threadPool" -> {
                DualWriteMetrics.ThreadPoolStatus poolStatus = metrics.getThreadPoolStatus();
                if (poolStatus != null) {
                    result.put("corePoolSize", poolStatus.corePoolSize());
                    result.put("maxPoolSize", poolStatus.maxPoolSize());
                    result.put("currentPoolSize", poolStatus.currentPoolSize());
                    result.put("activeCount", poolStatus.activeCount());
                    result.put("largestPoolSize", poolStatus.largestPoolSize());
                    result.put("completedTaskCount", poolStatus.completedTaskCount());
                    result.put("totalTaskCount", poolStatus.totalTaskCount());
                    result.put("poolUsageRate", String.format("%.2f%%", poolStatus.getPoolUsageRate()));
                    result.put("queueSize", poolStatus.queueSize());
                    result.put("queueRemainingCapacity", poolStatus.queueRemainingCapacity());
                    result.put("queueUsageRate", String.format("%.2f%%", poolStatus.getQueueUsageRate()));
                }
            }
            case "operations" -> {
                result.put("success", metrics.getOperationSuccessCount());
                result.put("failure", metrics.getOperationFailureCount());
            }
            default -> result.put("error", "Unknown type: " + type + ". Available: overview, threadPool, operations");
        }

        return result;
    }

    /**
     * 重置统计数据
     * DELETE /actuator/dualwrite
     */
    @DeleteOperation
    public Map<String, Object> reset() {
        metrics.reset();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("message", "Metrics reset successfully");
        return result;
    }
}
