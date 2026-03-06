package io.github.kk01001.redisson.template;

import io.github.kk01001.redisson.circuitbreaker.DualWriteCircuitBreaker;
import io.github.kk01001.redisson.monitor.DualWriteMetrics;
import io.github.kk01001.redisson.properties.MultiRedissonProperties;
import io.github.kk01001.redisson.retry.DualWriteFailureHandler;
import io.github.kk01001.redisson.retry.RetryTask;
import org.redisson.api.BatchOptions;
import org.redisson.api.BatchResult;
import org.redisson.api.RBatch;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RFuture;
import org.redisson.api.RListAsync;
import org.redisson.api.RMapAsync;
import org.redisson.api.RScoredSortedSetAsync;
import org.redisson.api.RSetAsync;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * @author kk01001
 * @date 2026-01-15 17:30:00
 * @description 批量操作封装类，支持 Pipeline 模式
 */
public class BatchOperations {

    private static final Logger log = LoggerFactory.getLogger(BatchOperations.class);

    private final RedissonClient primaryClient;
    private final RedissonClient secondaryClient;
    private final ExecutorService dualWriteExecutor;
    private final MultiRedissonProperties properties;
    private final DualWriteCircuitBreaker circuitBreaker;
    private final DualWriteMetrics metrics;
    private final DualWriteFailureHandler failureHandler;

    public BatchOperations(RedissonClient primaryClient,
                           RedissonClient secondaryClient,
                           ExecutorService dualWriteExecutor,
                           MultiRedissonProperties properties,
                           DualWriteCircuitBreaker circuitBreaker,
                           DualWriteMetrics metrics,
                           DualWriteFailureHandler failureHandler) {
        this.primaryClient = primaryClient;
        this.secondaryClient = secondaryClient;
        this.dualWriteExecutor = dualWriteExecutor;
        this.properties = properties;
        this.circuitBreaker = circuitBreaker;
        this.metrics = metrics;
        this.failureHandler = failureHandler;
    }

    /**
     * 是否启用双写（动态读取配置）
     */
    private boolean isDualWriteEnabled() {
        return properties.isDualWriteEnabled() && secondaryClient != null;
    }

    /**
     * 创建批量操作构建器
     */
    public BatchBuilder newBatch() {
        return new BatchBuilder(primaryClient.createBatch());
    }

    /**
     * 创建批量操作构建器（指定配置）
     */
    public BatchBuilder newBatch(BatchOptions options) {
        return new BatchBuilder(primaryClient.createBatch(options));
    }

    /**
     * 执行批量操作（使用 Consumer）
     */
    public BatchResult<?> executeBatch(Consumer<BatchBuilder> operations) {
        BatchBuilder builder = newBatch();
        operations.accept(builder);
        return builder.execute();
    }

    /**
     * 执行批量操作（原子模式）
     */
    public BatchResult<?> executeBatchAtomic(Consumer<BatchBuilder> operations) {
        BatchBuilder builder = newBatch(BatchOptions.defaults().executionMode(BatchOptions.ExecutionMode.REDIS_WRITE_ATOMIC));
        operations.accept(builder);
        return builder.execute();
    }

    /**
     * 批量操作构建器
     */
    public class BatchBuilder {

        private final RBatch batch;
        private final List<Runnable> secondaryOperations = new ArrayList<>();

        public BatchBuilder(RBatch batch) {
            this.batch = batch;
        }

        /**
         * 获取原始 RBatch 对象
         */
        public RBatch getRawBatch() {
            return batch;
        }

        // ==================== String 操作 ====================

        /**
         * 批量设置字符串值
         */
        public <V> BatchBuilder set(String key, V value) {
            batch.getBucket(key).setAsync(value);
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> secondaryClient.getBucket(key).set(value));
            }
            return this;
        }

        /**
         * 批量设置字符串值（带过期时间）
         */
        public <V> BatchBuilder set(String key, V value, Duration duration) {
            batch.getBucket(key).setAsync(value, duration);
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> secondaryClient.getBucket(key).set(value, duration));
            }
            return this;
        }

        /**
         * 批量获取字符串值
         */
        public <V> RFuture<V> get(String key) {
            RBucketAsync<V> bucket = batch.getBucket(key);
            return bucket.getAsync();
        }

        /**
         * 批量删除 Key
         */
        public BatchBuilder delete(String key) {
            batch.getBucket(key).deleteAsync();
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> secondaryClient.getBucket(key).delete());
            }
            return this;
        }

        // ==================== Hash 操作 ====================

        /**
         * 批量设置 Hash 字段
         */
        public <V> BatchBuilder hset(String key, String field, V value) {
            batch.getMap(key).putAsync(field, value);
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> secondaryClient.getMap(key).put(field, value));
            }
            return this;
        }

        /**
         * 批量设置多个 Hash 字段
         */
        public <K, V> BatchBuilder hmset(String key, Map<K, V> map) {
            batch.getMap(key).putAllAsync(map);
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> secondaryClient.getMap(key).putAll(map));
            }
            return this;
        }

        /**
         * 批量获取 Hash 字段
         */
        public <V> RFuture<V> hget(String key, String field) {
            RMapAsync<String, V> map = batch.getMap(key);
            return map.getAsync(field);
        }

        /**
         * 批量删除 Hash 字段
         */
        public BatchBuilder hdel(String key, Object... fields) {
            batch.getMap(key).fastRemoveAsync(fields);
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> secondaryClient.getMap(key).fastRemove(fields));
            }
            return this;
        }

        // ==================== Set 操作 ====================

        /**
         * 批量添加 Set 元素
         */
        @SafeVarargs
        public final <V> BatchBuilder sadd(String key, V... values) {
            batch.getSet(key).addAllAsync(List.of(values));
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> secondaryClient.getSet(key).addAll(List.of(values)));
            }
            return this;
        }

        /**
         * 批量移除 Set 元素
         */
        @SafeVarargs
        public final <V> BatchBuilder srem(String key, V... values) {
            RSetAsync<V> set = batch.getSet(key);
            for (V value : values) {
                set.removeAsync(value);
            }
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> {
                    for (V value : values) {
                        secondaryClient.getSet(key).remove(value);
                    }
                });
            }
            return this;
        }

        // ==================== List 操作 ====================

        /**
         * 批量左推入 List
         */
        @SafeVarargs
        public final <V> BatchBuilder lpush(String key, V... values) {
            RListAsync<V> list = batch.getList(key);
            for (V value : values) {
                list.addAsync(0, value);
            }
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> {
                    for (V value : values) {
                        secondaryClient.getList(key).add(0, value);
                    }
                });
            }
            return this;
        }

        /**
         * 批量右推入 List
         */
        @SafeVarargs
        public final <V> BatchBuilder rpush(String key, V... values) {
            batch.getList(key).addAllAsync(List.of(values));
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> secondaryClient.getList(key).addAll(List.of(values)));
            }
            return this;
        }

        // ==================== ZSet 操作 ====================

        /**
         * 批量添加 ZSet 元素
         */
        public <V> BatchBuilder zadd(String key, V value, double score) {
            batch.getScoredSortedSet(key).addAsync(score, value);
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> secondaryClient.getScoredSortedSet(key).add(score, value));
            }
            return this;
        }

        /**
         * 批量添加多个 ZSet 元素
         */
        public <V> BatchBuilder zaddAll(String key, Map<V, Double> values) {
            RScoredSortedSetAsync<V> sortedSet = batch.getScoredSortedSet(key);
            for (Map.Entry<V, Double> entry : values.entrySet()) {
                sortedSet.addAsync(entry.getValue(), entry.getKey());
            }
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> {
                    for (Map.Entry<V, Double> entry : values.entrySet()) {
                        secondaryClient.getScoredSortedSet(key).add(entry.getValue(), entry.getKey());
                    }
                });
            }
            return this;
        }

        /**
         * 批量移除 ZSet 元素
         */
        @SafeVarargs
        public final <V> BatchBuilder zrem(String key, V... values) {
            RScoredSortedSetAsync<V> sortedSet = batch.getScoredSortedSet(key);
            for (V value : values) {
                sortedSet.removeAsync(value);
            }
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> {
                    for (V value : values) {
                        secondaryClient.getScoredSortedSet(key).remove(value);
                    }
                });
            }
            return this;
        }

        // ==================== 通用操作 ====================

        /**
         * 批量设置过期时间
         */
        public BatchBuilder expire(String key, Duration duration) {
            batch.getBucket(key).expireAsync(duration);
            if (isDualWriteEnabled()) {
                secondaryOperations.add(() -> secondaryClient.getBucket(key).expire(duration));
            }
            return this;
        }

        /**
         * 执行批量操作
         */
        public BatchResult<?> execute() {
            BatchResult<?> result = batch.execute();

            if (isDualWriteEnabled() && !secondaryOperations.isEmpty()) {
                executeSecondaryOperations();
            }

            return result;
        }

        /**
         * 异步执行批量操作
         */
        public RFuture<BatchResult<?>> executeAsync() {
            RFuture<BatchResult<?>> future = batch.executeAsync();

            if (isDualWriteEnabled() && !secondaryOperations.isEmpty()) {
                future.whenComplete((batchResult, throwable) -> {
                    if (throwable == null) {
                        executeSecondaryOperations();
                    }
                });
            }

            return future;
        }

        /**
         * 执行备份集群操作（检查熔断器）
         */
        private void executeSecondaryOperations() {
            if (!circuitBreaker.allowRequest()) {
                log.debug("Circuit breaker is open, skip batch secondary write");
                if (failureHandler != null) {
                    Runnable batchAction = composeBatchAction();
                    failureHandler.onCircuitBreakerSkip(new RetryTask(batchAction, "batch"));
                }
                return;
            }

            Runnable batchAction = composeBatchAction();

            if (dualWriteExecutor != null) {
                try {
                    if (metrics != null) {
                        metrics.recordSubmit();
                    }
                    dualWriteExecutor.execute(() -> {
                        try {
                            batchAction.run();
                            if (metrics != null) {
                                metrics.recordSuccess("batch");
                            }
                            circuitBreaker.recordSuccess();
                        } catch (Exception e) {
                            log.error("Batch secondary write failed", e);
                            if (metrics != null) {
                                metrics.recordFailure("batch");
                            }
                            circuitBreaker.recordFailure();
                            if (failureHandler != null) {
                                failureHandler.onWriteFailure(new RetryTask(batchAction, "batch"));
                            }
                        }
                    });
                } catch (Exception e) {
                    log.error("Failed to submit batch secondary write task", e);
                    if (metrics != null) {
                        metrics.recordSubmitFailure();
                    }
                    circuitBreaker.recordFailure();
                }
            } else {
                try {
                    batchAction.run();
                } catch (Exception e) {
                    log.error("Batch secondary write failed (sync)", e);
                }
            }
        }

        /**
         * 将所有备份操作组合为一个 Runnable
         */
        private Runnable composeBatchAction() {
            List<Runnable> ops = new ArrayList<>(secondaryOperations);
            return () -> {
                for (Runnable op : ops) {
                    try {
                        op.run();
                    } catch (Exception e) {
                        log.error("Batch secondary operation failed", e);
                    }
                }
            };
        }

        /**
         * 丢弃批量操作
         */
        public void discard() {
            batch.discard();
            secondaryOperations.clear();
        }
    }
}
