package io.github.archer099.redisson.retry;

import io.github.archer099.redisson.circuitbreaker.CircuitBreakerState;
import io.github.archer099.redisson.circuitbreaker.CircuitBreakerStateChangeListener;
import io.github.archer099.redisson.circuitbreaker.DualWriteCircuitBreaker;
import io.github.archer099.redisson.monitor.DualWriteMetrics;
import io.github.archer099.redisson.properties.MultiRedissonProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author archer099
 * @date 2026-03-06 21:32:00
 * @description 默认的双写失败处理器，基于内存队列 + 后台线程重试
 * <p>
 * 失败的操作放入有界内存队列，后台调度线程定期消费并重试。
 * 超过最大重试次数的任务移入死信队列，仅记录日志。
 * </p>
 */
public class DefaultDualWriteFailureHandler implements DualWriteFailureHandler, CircuitBreakerStateChangeListener {

    private static final Logger log = LoggerFactory.getLogger(DefaultDualWriteFailureHandler.class);

    /**
     * 重试队列
     */
    private final BlockingQueue<RetryTask> retryQueue;

    /**
     * 死信队列
     */
    private final BlockingQueue<RetryTask> deadLetterQueue;

    /**
     * 重试调度线程池
     */
    private ScheduledExecutorService retryScheduler;

    /**
     * 熔断器引用（用于判断是否可以重试）
     */
    private final DualWriteCircuitBreaker circuitBreaker;

    /**
     * 监控指标
     */
    private final DualWriteMetrics metrics;

    /**
     * 配置
     */
    private final MultiRedissonProperties.RetryConfig retryConfig;

    /**
     * 是否已启动
     */
    private final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * 重试成功计数
     */
    private final LongAdder retrySuccessCount = new LongAdder();

    /**
     * 重试失败计数
     */
    private final LongAdder retryFailureCount = new LongAdder();

    /**
     * 入队丢弃计数（队列满）
     */
    private final LongAdder discardCount = new LongAdder();

    /**
     * 死信计数
     */
    private final LongAdder deadLetterCount = new LongAdder();

    /**
     * 队列溢出处理器
     */
    private final DualWriteOverflowHandler overflowHandler;

    public DefaultDualWriteFailureHandler(MultiRedissonProperties.RetryConfig retryConfig,
                                          DualWriteCircuitBreaker circuitBreaker,
                                          DualWriteMetrics metrics,
                                          DualWriteOverflowHandler overflowHandler) {
        this.retryConfig = retryConfig;
        this.circuitBreaker = circuitBreaker;
        this.metrics = metrics;
        this.overflowHandler = overflowHandler;
        this.retryQueue = new LinkedBlockingQueue<>(retryConfig.getQueueCapacity());
        this.deadLetterQueue = new LinkedBlockingQueue<>(retryConfig.getDeadLetterCapacity());
    }

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        circuitBreaker.addStateChangeListener(this);

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("dual-write-retry-" + counter.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };

        retryScheduler = new ScheduledThreadPoolExecutor(retryConfig.getWorkerThreads(), threadFactory);
        retryScheduler.scheduleWithFixedDelay(
                this::processRetryQueue,
                retryConfig.getRetryIntervalMs(),
                retryConfig.getRetryIntervalMs(),
                TimeUnit.MILLISECONDS
        );

        log.info("DefaultDualWriteFailureHandler started, queueCapacity={}, maxRetryCount={}, retryIntervalMs={}",
                retryConfig.getQueueCapacity(), retryConfig.getMaxRetryCount(), retryConfig.getRetryIntervalMs());
    }

    @Override
    public void onStateChange(CircuitBreakerState from, CircuitBreakerState to) {
        if (to == CircuitBreakerState.CLOSED && from != CircuitBreakerState.CLOSED) {
            int pending = retryQueue.size();
            if (pending > 0) {
                log.info("Circuit breaker recovered ({} -> CLOSED), triggering immediate retry of {} pending tasks",
                        from, pending);
                retryScheduler.execute(this::processRetryQueue);
            }
        }
    }

    @Override
    public void shutdown() {
        if (!started.compareAndSet(true, false)) {
            return;
        }

        circuitBreaker.removeStateChangeListener(this);

        if (retryScheduler != null) {
            retryScheduler.shutdown();
            try {
                if (!retryScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    retryScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                retryScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        int remaining = retryQueue.size();
        int deadLetters = deadLetterQueue.size();
        log.info("DefaultDualWriteFailureHandler shutdown, remainingTasks={}, deadLetters={}", remaining, deadLetters);
    }

    @Override
    public void onWriteFailure(RetryTask task) {
        enqueue(task, "writeFailure");
    }

    @Override
    public void onCircuitBreakerSkip(RetryTask task) {
        enqueue(task, "circuitBreakerSkip");
    }

    @Override
    public void onExecutorRejected(RetryTask task) {
        enqueue(task, "executorRejected");
    }

    @Override
    public void onMaxRetryExceeded(RetryTask task) {
        if (!deadLetterQueue.offer(task)) {
            log.error("Dead letter queue full, task permanently lost: {}", task);
        } else {
            deadLetterCount.increment();
            log.warn("Task moved to dead letter queue after {} retries: {}", task.getRetryCount(), task);
        }
    }

    /**
     * 入队
     */
    private void enqueue(RetryTask task, String reason) {
        if (!retryQueue.offer(task)) {
            discardCount.increment();
            if (overflowHandler != null) {
                try {
                    overflowHandler.onOverflow(task, reason, retryQueue.size());
                } catch (Exception e) {
                    log.error("Overflow handler failed for task: {}", task, e);
                }
            } else {
                log.warn("Retry queue full, task discarded (reason={}): {}", reason, task);
            }
            return;
        }
        log.debug("Task enqueued for retry (reason={}): {}", reason, task);
    }

    /**
     * 处理重试队列
     */
    private void processRetryQueue() {
        if (retryQueue.isEmpty()) {
            return;
        }

        int batchSize = Math.min(retryQueue.size(), retryConfig.getBatchSize());
        int processed = 0;

        for (int i = 0; i < batchSize; i++) {
            RetryTask task = retryQueue.poll();
            if (task == null) {
                break;
            }

            if (System.currentTimeMillis() < task.getNextRetryTime()) {
                retryQueue.offer(task);
                continue;
            }

            if (!circuitBreaker.allowRequest()) {
                task.setNextRetryTime(System.currentTimeMillis() + retryConfig.getRetryIntervalMs());
                retryQueue.offer(task);
                continue;
            }

            try {
                Runnable action = task.getAction();
                if (action == null) {
                    log.warn("RetryTask action is null, skipping: {}", task);
                    continue;
                }

                action.run();
                circuitBreaker.recordSuccess();
                retrySuccessCount.increment();
                processed++;

                if (metrics != null) {
                    metrics.recordSuccess(task.getActionName() + "_retry");
                }
            } catch (Exception e) {
                circuitBreaker.recordFailure();
                retryFailureCount.increment();

                task.incrementRetryCount();

                if (task.getRetryCount() >= retryConfig.getMaxRetryCount()) {
                    onMaxRetryExceeded(task);
                } else {
                    long delay = calculateBackoffDelay(task.getRetryCount());
                    task.setNextRetryTime(System.currentTimeMillis() + delay);
                    retryQueue.offer(task);
                }

                if (metrics != null) {
                    metrics.recordFailure(task.getActionName() + "_retry");
                }

                log.debug("Retry failed for task: {}, retryCount={}", task.getActionName(), task.getRetryCount(), e);
            }
        }

        if (processed > 0) {
            log.debug("Retry queue processed: {} tasks succeeded, queueRemaining={}", processed, retryQueue.size());
        }
    }

    /**
     * 计算退避延迟
     */
    private long calculateBackoffDelay(int retryCount) {
        long delay = (long) (retryConfig.getRetryIntervalMs() * Math.pow(retryConfig.getBackoffMultiplier(), retryCount));
        return Math.min(delay, retryConfig.getMaxRetryIntervalMs());
    }

    /**
     * 获取重试队列大小
     */
    public int getRetryQueueSize() {
        return retryQueue.size();
    }

    /**
     * 获取死信队列大小
     */
    public int getDeadLetterQueueSize() {
        return deadLetterQueue.size();
    }

    /**
     * 获取重试成功计数
     */
    public long getRetrySuccessCount() {
        return retrySuccessCount.sum();
    }

    /**
     * 获取重试失败计数
     */
    public long getRetryFailureCount() {
        return retryFailureCount.sum();
    }

    /**
     * 获取丢弃计数
     */
    public long getDiscardCount() {
        return discardCount.sum();
    }

    /**
     * 获取死信计数
     */
    public long getDeadLetterCount() {
        return deadLetterCount.sum();
    }
}
