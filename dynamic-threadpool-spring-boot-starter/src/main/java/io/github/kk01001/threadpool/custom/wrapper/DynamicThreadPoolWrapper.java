package io.github.kk01001.threadpool.custom.wrapper;

import com.alibaba.ttl.threadpool.TtlExecutors;
import io.github.kk01001.threadpool.actuator.ThreadPoolMetrics;
import io.github.kk01001.threadpool.alarm.ThreadPoolAlarmHandler;
import io.github.kk01001.threadpool.custom.model.ThreadPoolConfig;
import io.github.kk01001.threadpool.queue.ResizableLinkedBlockingQueue;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 动态线程池包装器
 * 包装 ThreadPoolExecutor，提供动态调整和监控能力
 *
 * @author kk01001
 */
@Slf4j
@Getter
public class DynamicThreadPoolWrapper {

    /**
     * 线程池名称
     */
    private final String poolName;

    /**
     * 线程池配置
     */
    private ThreadPoolConfig config;

    /**
     * 底层线程池执行器
     */
    private final MonitoredThreadPoolExecutor executor;

    /**
     * 拒绝次数计数器
     */
    private final AtomicLong rejectCount = new AtomicLong(0);

    /**
     * 告警处理器（可选）
     */
    @Setter
    private ThreadPoolAlarmHandler alarmHandler;

    public DynamicThreadPoolWrapper(String poolName, ThreadPoolConfig config) {
        this.poolName = poolName;
        this.config = config;
        this.executor = createThreadPoolExecutor(config);
        log.info("Dynamic thread pool [{}] created with config: {}", poolName, config);
    }

    /**
     * 创建线程池执行器
     */
    private MonitoredThreadPoolExecutor createThreadPoolExecutor(ThreadPoolConfig config) {
        config.validate();

        BlockingQueue<Runnable> workQueue = createWorkQueue(config);
        ThreadFactory threadFactory = createThreadFactory(config);
        RejectedExecutionHandler rejectedHandler = createRejectedHandler(config);

        MonitoredThreadPoolExecutor executor = new MonitoredThreadPoolExecutor(
                config.getCorePoolSize(),
                config.getMaxPoolSize(),
                config.getKeepAliveTime().toMillis(),
                TimeUnit.MILLISECONDS,
                workQueue,
                threadFactory,
                rejectedHandler
        );

        if (config.getAllowCoreThreadTimeout() != null && config.getAllowCoreThreadTimeout()) {
            executor.allowCoreThreadTimeOut(true);
        }

        // 如果启用 TTL，使用 TTL 包装器包装 Executor
        if (Boolean.TRUE.equals(config.getEnableTtl())) {
            return wrapWithTtl(executor);
        }

        return executor;
    }

    /**
     * 创建工作队列
     */
    private BlockingQueue<Runnable> createWorkQueue(ThreadPoolConfig config) {
        int capacity = config.getQueueCapacity();
        return switch (config.getQueueType()) {
            case RESIZABLE_LINKED_BLOCKING_QUEUE ->
                    new io.github.kk01001.threadpool.queue.ResizableLinkedBlockingQueue<>(capacity);
            case LINKED_BLOCKING_QUEUE -> new LinkedBlockingQueue<>(capacity);
            case ARRAY_BLOCKING_QUEUE -> new ArrayBlockingQueue<>(capacity);
            case SYNCHRONOUS_QUEUE -> new SynchronousQueue<>();
            case PRIORITY_BLOCKING_QUEUE -> new PriorityBlockingQueue<>(capacity);
        };
    }

    /**
     * 创建线程工厂
     */
    private ThreadFactory createThreadFactory(ThreadPoolConfig config) {
        AtomicLong threadNumber = new AtomicLong(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(config.getThreadNamePrefix() + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        };
    }

    /**
     * 使用 TTL 包装 Executor
     */
    private MonitoredThreadPoolExecutor wrapWithTtl(MonitoredThreadPoolExecutor executor) {
        try {
            // 并记录 TTL 已启用
            log.info("TTL wrapper enabled for thread pool [{}]", config.getPoolName());
            // 注意：TTL 的包装是在任务提交时自动处理的，不需要替换 executor
            return (MonitoredThreadPoolExecutor) TtlExecutors.getTtlExecutor(executor);
        } catch (Exception e) {
            log.warn("Failed to wrap executor with TTL, using basic executor. Error: {}", e.getMessage());
            return executor;
        }
    }

    /**
     * 创建拒绝策略处理器（带计数）
     */
    private RejectedExecutionHandler createRejectedHandler(ThreadPoolConfig config) {
        RejectedExecutionHandler originalHandler = config.getRejectedExecutionHandler();
        return (r, executor) -> {
            rejectCount.incrementAndGet();
            log.warn("Thread pool [{}] rejected task, reject count: {}", poolName, rejectCount.get());
            originalHandler.rejectedExecution(r, executor);
        };
    }

    /**
     * 动态更新线程池配置
     */
    public synchronized void updateConfig(ThreadPoolConfig newConfig) {
        newConfig.validate();

        String oldConfigStr = formatConfig(config);
        String newConfigStr = formatConfig(newConfig);

        log.info("Updating thread pool [{}] config from [{}] to [{}]", poolName, oldConfigStr, newConfigStr);

        // 更新核心线程数
        if (!newConfig.getCorePoolSize().equals(config.getCorePoolSize())) {
            executor.setCorePoolSize(newConfig.getCorePoolSize());
            log.info("Thread pool [{}] core pool size updated: {} -> {}",
                    poolName, config.getCorePoolSize(), newConfig.getCorePoolSize());
        }

        // 更新最大线程数
        if (!newConfig.getMaxPoolSize().equals(config.getMaxPoolSize())) {
            executor.setMaximumPoolSize(newConfig.getMaxPoolSize());
            log.info("Thread pool [{}] max pool size updated: {} -> {}",
                    poolName, config.getMaxPoolSize(), newConfig.getMaxPoolSize());
        }

        // 更新线程存活时间
        if (!newConfig.getKeepAliveTime().equals(config.getKeepAliveTime())) {
            executor.setKeepAliveTime(newConfig.getKeepAliveTime().toMillis(), TimeUnit.MILLISECONDS);
            log.info("Thread pool [{}] keep alive time updated: {} -> {}",
                    poolName, config.getKeepAliveTime(), newConfig.getKeepAliveTime());
        }

        // 更新核心线程超时设置
        if (newConfig.getAllowCoreThreadTimeout() != null &&
                !newConfig.getAllowCoreThreadTimeout().equals(config.getAllowCoreThreadTimeout())) {
            executor.allowCoreThreadTimeOut(newConfig.getAllowCoreThreadTimeout());
            log.info("Thread pool [{}] allow core thread timeout updated: {} -> {}",
                    poolName, config.getAllowCoreThreadTimeout(), newConfig.getAllowCoreThreadTimeout());
        }

        // 更新队列容量（如果使用的是可调整容量的队列）
        if (!newConfig.getQueueCapacity().equals(config.getQueueCapacity())) {
            BlockingQueue<Runnable> queue = executor.getQueue();
            if (queue instanceof ResizableLinkedBlockingQueue resizableQueue) {
                resizableQueue.setCapacity(newConfig.getQueueCapacity());
                log.info("Thread pool [{}] queue capacity updated: {} -> {}",
                        poolName, config.getQueueCapacity(), newConfig.getQueueCapacity());
            } else {
                log.warn("Thread pool [{}] queue capacity change detected: {} -> {}, but current queue type [{}] does not support dynamic resizing. " +
                                "Only RESIZABLE_LINKED_BLOCKING_QUEUE supports dynamic capacity modification.",
                        poolName, config.getQueueCapacity(), newConfig.getQueueCapacity(), config.getQueueType());
            }
        }

        this.config = newConfig;
        log.info("Thread pool [{}] configuration updated successfully", poolName);

        // 发送配置变更告警
        if (alarmHandler != null) {
            alarmHandler.sendConfigChangeAlarm(poolName, oldConfigStr, newConfigStr);
        }
    }

    /**
     * 格式化配置为字符串
     */
    private String formatConfig(ThreadPoolConfig config) {
        return String.format("core=%d, max=%d, queue=%d, keepAlive=%s",
                config.getCorePoolSize(),
                config.getMaxPoolSize(),
                config.getQueueCapacity(),
                config.getKeepAliveTime());
    }

    /**
     * 收集线程池指标
     */
    public ThreadPoolMetrics collectMetrics() {
        BlockingQueue<Runnable> queue = executor.getQueue();
        int queueCapacity = queue.remainingCapacity() + queue.size();

        return ThreadPoolMetrics.builder()
                .poolName(poolName)
                .corePoolSize(executor.getCorePoolSize())
                .maxPoolSize(executor.getMaximumPoolSize())
                .activeThreadCount(executor.getActiveCount())
                .poolSize(executor.getPoolSize())
                .largestPoolSize(executor.getLargestPoolSize())
                .queueCapacity(queueCapacity)
                .queueSize(queue.size())
                .queueRemainingCapacity(queue.remainingCapacity())
                .completedTaskCount(executor.getCompletedTaskCount())
                .taskCount(executor.getTaskCount())
                .rejectCount(rejectCount.get())
                .queueUsageRatio(queueCapacity > 0 ? (double) queue.size() / queueCapacity : 0.0)
                .activeThreadRatio(executor.getMaximumPoolSize() > 0 ?
                        (double) executor.getActiveCount() / executor.getMaximumPoolSize() : 0.0)
                .shutdown(executor.isShutdown())
                .terminated(executor.isTerminated())
                .collectTime(LocalDateTime.now())
                .keepAliveTime(config.getKeepAliveTime().toSeconds())
                .rejectedPolicyType(config.getRejectedPolicyType() != null ? config.getRejectedPolicyType().toString() : null)
                .allowCoreThreadTimeout(config.getAllowCoreThreadTimeout())
                .threadNamePrefix(config.getThreadNamePrefix())
                .queueType(config.getQueueType() != null ? config.getQueueType().toString() : null)
                .build();
    }

    /**
     * 提交任务
     */
    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    /**
     * 提交任务
     */
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * 执行任务
     */
    public void execute(Runnable command) {
        executor.execute(command);
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        log.info("Shutting down thread pool [{}]", poolName);
        executor.shutdown();
    }

    /**
     * 立即关闭线程池
     */
    public void shutdownNow() {
        log.warn("Force shutting down thread pool [{}]", poolName);
        executor.shutdownNow();
    }

    /**
     * 等待终止
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    /**
     * 监控的线程池执行器
     */
    private static class MonitoredThreadPoolExecutor extends ThreadPoolExecutor {
        public MonitoredThreadPoolExecutor(int corePoolSize,
                                           int maximumPoolSize,
                                           long keepAliveTime,
                                           TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue,
                                           ThreadFactory threadFactory,
                                           RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }
    }
}
