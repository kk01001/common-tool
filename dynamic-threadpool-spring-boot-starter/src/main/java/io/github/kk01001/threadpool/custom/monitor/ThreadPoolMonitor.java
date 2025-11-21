package io.github.kk01001.threadpool.custom.monitor;

import io.github.kk01001.threadpool.actuator.ThreadPoolMetrics;
import io.github.kk01001.threadpool.custom.config.DynamicThreadPoolProperties;
import io.github.kk01001.threadpool.registry.ThreadPoolRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;

/**
 * 线程池监控器
 * 定时采集线程池指标并发布
 *
 * @author kk01001
 */
@Slf4j
public class ThreadPoolMonitor {

    private final ThreadPoolRegistry registry;
    private final DynamicThreadPoolProperties properties;
    private final MeterRegistry meterRegistry;

    public ThreadPoolMonitor(ThreadPoolRegistry registry,
                             DynamicThreadPoolProperties properties,
                             MeterRegistry meterRegistry) {
        this.registry = registry;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    /**
     * 定时采集指标（包括第三方线程池）
     */
    @Scheduled(fixedDelayString = "#{@dynamicThreadPoolProperties.monitor.collectInterval.toMillis()}")
    public void collectMetrics() {
        if (!properties.getMonitor().getEnabled()) {
            return;
        }

        // 收集所有线程池指标（包括业务线程池和第三方线程池）
        Map<String, ThreadPoolMetrics> metricsMap = registry.collectAllMetricsIncludingThirdParty();
        metricsMap.forEach((poolName, metrics) -> {
            try {
                publishMetrics(metrics);
            } catch (Exception e) {
                log.error("Failed to publish metrics for thread pool [{}]", poolName, e);
            }
        });
    }

    /**
     * 发布指标到 Micrometer
     */
    private void publishMetrics(ThreadPoolMetrics metrics) {
        if (!properties.getMonitor().getEnableMetrics() || meterRegistry == null) {
            return;
        }

        List<Tag> tags = List.of(Tag.of("pool.name", metrics.getPoolName()));

        // 核心线程数
        meterRegistry.gauge("threadpool.core.size", tags, metrics.getCorePoolSize());

        // 最大线程数
        meterRegistry.gauge("threadpool.max.size", tags, metrics.getMaxPoolSize());

        // 当前线程数
        meterRegistry.gauge("threadpool.pool.size", tags, metrics.getPoolSize());

        // 活跃线程数
        meterRegistry.gauge("threadpool.active.count", tags, metrics.getActiveThreadCount());

        // 队列大小
        meterRegistry.gauge("threadpool.queue.size", tags, metrics.getQueueSize());

        // 队列容量
        meterRegistry.gauge("threadpool.queue.capacity", tags, metrics.getQueueCapacity());

        // 队列使用率
        meterRegistry.gauge("threadpool.queue.usage.ratio", tags, metrics.getQueueUsageRatio());

        // 活跃线程比例
        meterRegistry.gauge("threadpool.active.ratio", tags, metrics.getActiveThreadRatio());

        // 已完成任务数
        meterRegistry.gauge("threadpool.completed.task.count", tags, metrics.getCompletedTaskCount());

        // 总任务数
        meterRegistry.gauge("threadpool.task.count", tags, metrics.getTaskCount());

        // 拒绝次数
        meterRegistry.gauge("threadpool.reject.count", tags, metrics.getRejectCount());
    }

    /**
     * 获取所有线程池指标（包括第三方）
     */
    public Map<String, ThreadPoolMetrics> getAllMetrics() {
        return registry.collectAllMetricsIncludingThirdParty();
    }

    /**
     * 获取指定线程池指标
     */
    public ThreadPoolMetrics getMetrics(String poolName) {
        var wrapper = registry.getThreadPool(poolName);
        return wrapper != null ? wrapper.collectMetrics() : null;
    }
}
