package io.github.archer099.threadpool.custom.model;

import io.github.archer099.threadpool.custom.config.DynamicThreadPoolProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置模型
 *
 * @author archer099
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolConfig {

    /**
     * 线程池名称
     */
    private String poolName;

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maxPoolSize;

    /**
     * 队列容量
     */
    private Integer queueCapacity;

    /**
     * 队列类型
     */
    private DynamicThreadPoolProperties.QueueType queueType;

    /**
     * 线程存活时间
     */
    private Duration keepAliveTime;

    /**
     * 线程名称前缀
     */
    private String threadNamePrefix;

    /**
     * 拒绝策略
     */
    private DynamicThreadPoolProperties.RejectedPolicyType rejectedPolicyType;

    /**
     * 是否允许核心线程超时
     */
    private Boolean allowCoreThreadTimeout;

    /**
     * 是否启用 TTL
     */
    private Boolean enableTtl;

    /**
     * 获取实际的拒绝策略处理器
     */
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return switch (rejectedPolicyType) {
            case ABORT_POLICY -> new ThreadPoolExecutor.AbortPolicy();
            case DISCARD_OLDEST_POLICY -> new ThreadPoolExecutor.DiscardOldestPolicy();
            case DISCARD_POLICY -> new ThreadPoolExecutor.DiscardPolicy();
            default -> new ThreadPoolExecutor.CallerRunsPolicy();
        };
    }

    /**
     * 验证配置有效性
     */
    public void validate() {
        if (corePoolSize <= 0) {
            throw new IllegalArgumentException("corePoolSize must be greater than 0");
        }
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("maxPoolSize must be greater than 0");
        }
        if (maxPoolSize < corePoolSize) {
            throw new IllegalArgumentException("maxPoolSize must be greater than or equal to corePoolSize");
        }
        if (queueCapacity < 0) {
            throw new IllegalArgumentException("queueCapacity must be greater than or equal to 0");
        }
    }
}
