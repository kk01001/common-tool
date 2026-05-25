package io.github.archer099.threadpool.actuator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 线程池指标
 *
 * @author archer099
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolMetrics {

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
     * 当前活跃线程数
     */
    private Integer activeThreadCount;

    /**
     * 当前线程池大小
     */
    private Integer poolSize;

    /**
     * 最大曾出现的线程数
     */
    private Integer largestPoolSize;

    /**
     * 队列容量
     */
    private Integer queueCapacity;

    /**
     * 当前队列大小
     */
    private Integer queueSize;

    /**
     * 队列剩余容量
     */
    private Integer queueRemainingCapacity;

    /**
     * 已完成任务数
     */
    private Long completedTaskCount;

    /**
     * 总任务数
     */
    private Long taskCount;

    /**
     * 拒绝次数
     */
    private Long rejectCount;

    /**
     * 队列使用率（0-1）
     */
    private Double queueUsageRatio;

    /**
     * 活跃线程比例（0-1）
     */
    private Double activeThreadRatio;

    /**
     * 是否已关闭
     */
    private Boolean shutdown;

    /**
     * 是否已终止
     */
    private Boolean terminated;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;

    /**
     * 线程存活时间（秒）
     */
    private Long keepAliveTime;

    /**
     * 拒绝策略类型
     */
    private String rejectedPolicyType;

    /**
     * 是否允许核心线程超时
     */
    private Boolean allowCoreThreadTimeout;

    /**
     * 线程名称前缀
     */
    private String threadNamePrefix;

    /**
     * 队列类型
     */
    private String queueType;
}
