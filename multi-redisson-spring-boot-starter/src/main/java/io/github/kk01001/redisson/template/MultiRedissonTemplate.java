package io.github.kk01001.redisson.template;

import io.github.kk01001.redisson.circuitbreaker.DualWriteCircuitBreaker;
import io.github.kk01001.redisson.holder.RedissonClientHolder;
import io.github.kk01001.redisson.monitor.DualWriteMetrics;
import io.github.kk01001.redisson.properties.MultiRedissonProperties;
import org.redisson.api.GeoEntry;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoPosition;
import org.redisson.api.GeoUnit;
import org.redisson.api.RAtomicDouble;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBitSet;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RDeque;
import org.redisson.api.RGeo;
import org.redisson.api.RHyperLogLog;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.api.geo.GeoSearchArgs;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.ScoredEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author kk01001
 * @date 2026-01-15 14:00:00
 * @description Redisson 多集群操作模板，支持双写
 * <p>
 * 对外提供统一的 Redis 操作接口，内部自动处理双写逻辑：
 * - 主集群：同步操作
 * - 备份集群：异步操作（通过线程池）
 * </p>
 */
@SuppressWarnings("all")
public class MultiRedissonTemplate {

    private static final Logger log = LoggerFactory.getLogger(MultiRedissonTemplate.class);

    /**
     * 主 RedissonClient
     */
    private final RedissonClient primaryClient;

    /**
     * 备份 RedissonClient（可为空）
     */
    private final RedissonClient secondaryClient;

    /**
     * 双写线程池
     */
    private final ExecutorService dualWriteExecutor;

    /**
     * 配置属性（用于动态读取配置）
     */
    private final MultiRedissonProperties properties;

    /**
     * 双写监控指标
     */
    private final DualWriteMetrics metrics;

    /**
     * 双写熔断器
     */
    private final DualWriteCircuitBreaker circuitBreaker;

    /**
     * 批量操作
     */
    private final BatchOperations batchOperations;

    public MultiRedissonTemplate(RedissonClientHolder holder,
                                 MultiRedissonProperties properties,
                                 ExecutorService dualWriteExecutor,
                                 DualWriteMetrics metrics,
                                 DualWriteCircuitBreaker circuitBreaker) {
        this.properties = properties;
        this.primaryClient = holder.getPrimary();
        this.metrics = metrics;
        this.circuitBreaker = circuitBreaker;
        this.dualWriteExecutor = dualWriteExecutor;

        // 获取备份客户端（如果配置了）
        if (StringUtils.hasText(properties.getSecondary())) {
            this.secondaryClient = holder.getClientOrNull(properties.getSecondary());
            if (this.secondaryClient == null) {
                log.warn("Secondary client '{}' not found, dual write will be disabled",
                        properties.getSecondary());
            }
        } else {
            this.secondaryClient = null;
        }

        // 初始化批量操作（传入 properties 支持动态配置）
        this.batchOperations = new BatchOperations(primaryClient, secondaryClient, 
                dualWriteExecutor, properties);

        log.info("MultiRedissonTemplate initialized, dualWriteEnabled: {}, primary: {}, secondary: {}, circuitBreaker: {}",
                isDualWriteEnabled(),
                properties.getPrimary(),
                properties.getSecondary(),
                properties.getCircuitBreaker().isEnabled());
    }

    /**
     * 获取监控指标
     */
    public DualWriteMetrics getMetrics() {
        return metrics;
    }

    /**
     * 获取熔断器
     */
    public DualWriteCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    /**
     * 获取批量操作
     */
    public BatchOperations batch() {
        return batchOperations;
    }

    /**
     * 获取主 RedissonClient
     */
    public RedissonClient getPrimaryClient() {
        return primaryClient;
    }

    /**
     * 获取备份 RedissonClient
     */
    public RedissonClient getSecondaryClient() {
        return secondaryClient;
    }

    /**
     * 是否启用双写（动态读取配置）
     */
    public boolean isDualWriteEnabled() {
        return properties.isDualWriteEnabled() && secondaryClient != null;
    }

    // ====================== String 操作 ======================

    /**
     * 设置字符串值
     */
    public <V> Boolean set(String key, V value) {
        return write(() -> {
            primaryClient.getBucket(key).set(value);
            return true;
        }, () -> secondaryClient.getBucket(key).set(value), "set");
    }

    /**
     * 设置字符串值（指定编码器）
     */
    public <V> Boolean set(Codec codec, String key, V value) {
        return write(() -> {
            primaryClient.getBucket(key, codec).set(value);
            return true;
        }, () -> secondaryClient.getBucket(key, codec).set(value), "setWithCodec");
    }

    /**
     * 设置字符串值和过期时间
     */
    public <V> Boolean set(String key, V value, Duration duration) {
        return write(() -> {
            primaryClient.getBucket(key).set(value, duration);
            return true;
        }, () -> secondaryClient.getBucket(key).set(value, duration), "setWithExpire");
    }

    /**
     * 设置字符串值和过期时间（指定编码器）
     */
    public <V> Boolean set(Codec codec, String key, V value, Duration duration) {
        return write(() -> {
            primaryClient.getBucket(key, codec).set(value, duration);
            return true;
        }, () -> secondaryClient.getBucket(key, codec).set(value, duration), "setWithCodecAndExpire");
    }

    /**
     * 设置锁（如果不存在）
     */
    public <V> boolean setNx(String key, V value, Duration duration) {
        RBucket<V> bucket = primaryClient.getBucket(key);
        boolean result = bucket.setIfAbsent(value, duration);
        if (result && isDualWriteEnabled()) {
            asyncWrite(() -> secondaryClient.getBucket(key).setIfAbsent(value, duration), "setNx");
        }
        return result;
    }

    /**
     * 设置锁（如果不存在，指定编码器）
     */
    public <V> boolean setNx(Codec codec, String key, V value, Duration duration) {
        RBucket<V> bucket = primaryClient.getBucket(key, codec);
        boolean result = bucket.setIfAbsent(value, duration);
        if (result && isDualWriteEnabled()) {
            asyncWrite(() -> secondaryClient.getBucket(key, codec).setIfAbsent(value, duration), "setNxWithCodec");
        }
        return result;
    }

    /**
     * 获取字符串值
     */
    public <V> V get(String key) {
        RBucket<V> bucket = primaryClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 使用指定编码器获取值
     */
    public <V> V get(Codec codec, String key) {
        RBucket<V> bucket = primaryClient.getBucket(key, codec);
        return bucket.get();
    }

    /**
     * 获取并设置新值
     */
    public <V> V getAndSet(String key, V value) {
        return writeWithResult(() -> {
            RBucket<V> bucket = primaryClient.getBucket(key);
            return bucket.getAndSet(value);
        }, result -> secondaryClient.getBucket(key).set(value), "getAndSet");
    }

    /**
     * 获取并设置新值（指定编码器）
     */
    public <V> V getAndSet(Codec codec, String key, V value) {
        return writeWithResult(() -> {
            RBucket<V> bucket = primaryClient.getBucket(key, codec);
            return bucket.getAndSet(value);
        }, result -> secondaryClient.getBucket(key, codec).set(value), "getAndSetWithCodec");
    }

    /**
     * 获取并删除
     */
    public <V> V getAndDelete(String key) {
        return writeWithResult(() -> {
            RBucket<V> bucket = primaryClient.getBucket(key);
            return bucket.getAndDelete();
        }, result -> secondaryClient.getBucket(key).delete(), "getAndDelete");
    }

    /**
     * 获取并删除（指定编码器）
     */
    public <V> V getAndDelete(Codec codec, String key) {
        return writeWithResult(() -> {
            RBucket<V> bucket = primaryClient.getBucket(key, codec);
            return bucket.getAndDelete();
        }, result -> secondaryClient.getBucket(key, codec).delete(), "getAndDeleteWithCodec");
    }

    // ====================== Hash 操作 ======================

    /**
     * 设置 hash 字段
     */
    public <V> void hset(String key, String field, V value) {
        write(() -> {
            RMap<String, V> map = primaryClient.getMap(key);
            map.put(field, value);
            return null;
        }, () -> {
            RMap<String, V> map = secondaryClient.getMap(key);
            map.put(field, value);
        }, "hset");
    }

    /**
     * 批量设置 hash 字段
     */
    public <V> void hmset(String key, Map<String, V> map) {
        write(() -> {
            RMap<String, V> rMap = primaryClient.getMap(key);
            rMap.putAll(map);
            return null;
        }, () -> {
            RMap<String, V> rMap = secondaryClient.getMap(key);
            rMap.putAll(map);
        }, "hmset");
    }

    /**
     * 获取 hash 字段值
     */
    public <V> V hget(String key, String field) {
        RMap<String, V> map = primaryClient.getMap(key);
        return map.get(field);
    }

    /**
     * 获取所有 hash 字段和值
     */
    public <K, V> Map<K, V> hgetAll(String key) {
        RMap<K, V> map = primaryClient.getMap(key);
        return map.readAllMap();
    }

    /**
     * 获取 hash 大小
     */
    public int hsize(String key) {
        RMap<?, ?> map = primaryClient.getMap(key);
        return map.size();
    }

    /**
     * 删除 hash 字段
     */
    public Long hdel(String key, String... fields) {
        return write(() -> {
            RMap<String, ?> map = primaryClient.getMap(key);
            return map.fastRemove(fields);
        }, () -> {
            RMap<String, ?> map = secondaryClient.getMap(key);
            map.fastRemove(fields);
        }, "hdel");
    }

    /**
     * hash 字段递增
     */
    public <V> V hincrby(String key, String field, Number value) {
        return write(() -> {
            RMap<Object, V> rMap = primaryClient.getMap(key);
            return rMap.addAndGet(field, value);
        }, () -> secondaryClient.getMap(key).addAndGet(field, value), "hincrby");
    }

    // ====================== Set 操作 ======================

    /**
     * 添加 Set 元素
     */
    @SafeVarargs
    public final <V> Boolean sadd(String key, V... values) {
        return write(() -> {
            RSet<V> set = primaryClient.getSet(key);
            return set.addAll(Arrays.asList(values));
        }, () -> {
            RSet<V> set = secondaryClient.getSet(key);
            set.addAll(Arrays.asList(values));
        }, "sadd");
    }

    /**
     * 添加 Set 元素（List）
     */
    public <V> Boolean saddAll(String key, List<V> values) {
        return write(() -> {
            RSet<V> set = primaryClient.getSet(key);
            return set.addAll(values);
        }, () -> {
            RSet<V> set = secondaryClient.getSet(key);
            set.addAll(values);
        }, "saddAll");
    }

    /**
     * 移除 Set 元素
     */
    @SafeVarargs
    public final <V> Boolean srem(String key, V... values) {
        return write(() -> {
            RSet<V> set = primaryClient.getSet(key);
            return set.removeAll(Arrays.asList(values));
        }, () -> {
            RSet<V> set = secondaryClient.getSet(key);
            set.removeAll(Arrays.asList(values));
        }, "srem");
    }

    /**
     * 获取 Set 所有元素
     */
    public <V> Set<V> smembers(String key) {
        RSet<V> set = primaryClient.getSet(key);
        return set.readAll();
    }

    /**
     * 判断元素是否在 Set 中
     */
    public <V> Boolean sismember(String key, V value) {
        RSet<V> set = primaryClient.getSet(key);
        return set.contains(value);
    }

    /**
     * 随机获取一个元素（不移除）
     */
    public <V> V sRandom(String key) {
        RSet<V> set = primaryClient.getSet(key);
        return set.random();
    }

    /**
     * 随机移除并返回一个元素
     * <p>
     * 注意：第二集群会删除主集群返回的指定元素
     * </p>
     */
    public <V> V spop(String key) {
        return writeWithResult(() -> {
            RSet<V> set = primaryClient.getSet(key);
            return set.removeRandom();
        }, data -> {
            if (data != null) {
                RSet<V> set = secondaryClient.getSet(key);
                set.remove(data);
            }
        }, "spop");
    }

    /**
     * 随机移除并返回多个元素
     * <p>
     * 注意：第二集群会删除主集群返回的指定元素
     * </p>
     */
    public <V> Set<V> spop(String key, int count) {
        return writeWithResult(() -> {
            RSet<V> set = primaryClient.getSet(key);
            return set.removeRandom(count);
        }, data -> {
            if (data != null && !data.isEmpty()) {
                RSet<V> set = secondaryClient.getSet(key);
                set.removeAll(data);
            }
        }, "spopCount");
    }

    /**
     * 获取 Set 大小
     */
    public int scard(String key) {
        RSet<?> set = primaryClient.getSet(key);
        return set.size();
    }

    // ====================== List 操作 ======================

    /**
     * 从左侧添加 List 元素（头部插入）
     */
    @SafeVarargs
    public final <V> Boolean lpush(String key, V... values) {
        return write(() -> {
            RList<V> list = primaryClient.getList(key);
            return list.addAll(0, Arrays.asList(values));
        }, () -> {
            RList<V> list = secondaryClient.getList(key);
            list.addAll(0, Arrays.asList(values));
        }, "lpush");
    }

    /**
     * 从右侧添加 List 元素（尾部插入）
     */
    @SafeVarargs
    public final <V> Boolean rpush(String key, V... values) {
        return write(() -> {
            RList<V> list = primaryClient.getList(key);
            return list.addAll(Arrays.asList(values));
        }, () -> {
            RList<V> list = secondaryClient.getList(key);
            list.addAll(Arrays.asList(values));
        }, "rpush");
    }

    /**
     * 从左侧批量添加 List 元素
     */
    public <V> Boolean lpushAll(String key, List<V> values) {
        return write(() -> {
            RList<V> list = primaryClient.getList(key);
            return list.addAll(0, values);
        }, () -> {
            RList<V> list = secondaryClient.getList(key);
            list.addAll(0, values);
        }, "lpushAll");
    }

    /**
     * 从右侧批量添加 List 元素
     */
    public <V> Boolean rpushAll(String key, List<V> values) {
        return write(() -> {
            RList<V> list = primaryClient.getList(key);
            return list.addAll(values);
        }, () -> {
            RList<V> list = secondaryClient.getList(key);
            list.addAll(values);
        }, "rpushAll");
    }

    /**
     * 查询全部 List
     */
    public <V> List<V> lrange(String key) {
        RList<V> list = primaryClient.getList(key);
        return list.readAll();
    }

    /**
     * 获取 List 指定范围的元素
     */
    public <V> List<V> lrange(String key, int start, int end) {
        RList<V> list = primaryClient.getList(key);
        return list.range(start, end);
    }

    /**
     * 移除 List 中的元素
     */
    public <V> Boolean lrem(String key, V value) {
        return write(() -> {
            RList<V> list = primaryClient.getList(key);
            return list.remove(value);
        }, () -> {
            RList<V> list = secondaryClient.getList(key);
            list.remove(value);
        }, "lrem");
    }

    /**
     * 批量移除 List 元素
     */
    public <V> Boolean lremAll(String key, List<V> values) {
        return write(() -> {
            RList<V> list = primaryClient.getList(key);
            return list.removeAll(values);
        }, () -> {
            RList<V> list = secondaryClient.getList(key);
            list.removeAll(values);
        }, "lremAll");
    }

    /**
     * 获取 List 大小
     */
    public int lsize(String key) {
        RList<?> list = primaryClient.getList(key);
        return list.size();
    }

    // ====================== ZSet 操作 ======================

    /**
     * 添加有序集合元素
     */
    public <V> Boolean zadd(String key, V value, double score) {
        return write(() -> {
            RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
            return zset.add(score, value);
        }, () -> {
            RScoredSortedSet<V> zset = secondaryClient.getScoredSortedSet(key);
            zset.add(score, value);
        }, "zadd");
    }

    /**
     * 批量添加有序集合元素
     */
    public <V> int zaddAll(String key, Map<V, Double> values) {
        return write(() -> {
            RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
            return zset.addAll(values);
        }, () -> {
            RScoredSortedSet<V> zset = secondaryClient.getScoredSortedSet(key);
            zset.addAll(values);
        }, "zaddAll");
    }

    /**
     * 获取有序集合指定分数范围的元素
     */
    public <V> Collection<V> zrangeByScore(String key, double min, double max) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.valueRange(min, true, max, true);
    }

    /**
     * 获取元素的分数
     */
    public <V> Double zscore(String key, V value) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.getScore(value);
    }

    /**
     * 获取元素排名（从大到小）
     */
    public <V> Integer zrevrank(String key, V value) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.revRank(value);
    }

    /**
     * 按分数从大到小返回元素
     */
    public <V> Collection<V> zrevrange(String key, int start, int end) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.valueRangeReversed(start, end);
    }

    /**
     * 获取所有元素和分数
     */
    public <V> Collection<ScoredEntry<V>> zgetAllWithScores(String key) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.entryRange(0, -1);
    }

    /**
     * 移除有序集合中的元素
     */
    @SafeVarargs
    public final <V> boolean zrem(String key, V... values) {
        return write(() -> {
            RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
            return zset.removeAll(Arrays.asList(values));
        }, () -> {
            RScoredSortedSet<V> zset = secondaryClient.getScoredSortedSet(key);
            zset.removeAll(Arrays.asList(values));
        }, "zrem");
    }

    /**
     * 获取有序集合大小
     */
    public int zcard(String key) {
        RScoredSortedSet<?> zset = primaryClient.getScoredSortedSet(key);
        return zset.size();
    }

    /**
     * 增加元素分数
     */
    public <V> Double zincrby(String key, V value, double delta) {
        return write(() -> {
            RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
            return zset.addScore(value, delta);
        }, () -> {
            RScoredSortedSet<V> zset = secondaryClient.getScoredSortedSet(key);
            zset.addScore(value, delta);
        }, "zincrby");
    }

    /**
     * 弹出分数最高的元素
     */
    public <V> V zpopMax(String key) {
        return writeWithResult(() -> {
            RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
            return zset.pollLast();
        }, data -> {
            if (data != null) {
                RScoredSortedSet<V> zset = secondaryClient.getScoredSortedSet(key);
                zset.remove(data);
            }
        }, "zpopMax");
    }

    /**
     * 弹出分数最低的元素
     */
    public <V> V zpopMin(String key) {
        return writeWithResult(() -> {
            RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
            return zset.pollFirst();
        }, data -> {
            if (data != null) {
                RScoredSortedSet<V> zset = secondaryClient.getScoredSortedSet(key);
                zset.remove(data);
            }
        }, "zpopMin");
    }

    // ====================== Deque 操作 ======================

    /**
     * 添加元素到队尾
     */
    public <V> Boolean offerLast(String key, V value) {
        return write(() -> primaryClient.getDeque(key).offerLast(value),
                () -> secondaryClient.getDeque(key).offerLast(value), "offerLast");
    }

    /**
     * 添加元素到队首
     */
    public <V> Boolean offerFirst(String key, V value) {
        return write(() -> primaryClient.getDeque(key).offerFirst(value),
                () -> secondaryClient.getDeque(key).offerFirst(value), "offerFirst");
    }

    /**
     * 获取并移除队首元素
     */
    public <V> V pollFirst(String key) {
        return writeWithResult(() -> {
            RDeque<V> deque = primaryClient.getDeque(key);
            return deque.pollFirst();
        }, data -> {
            if (data != null) {
                secondaryClient.getDeque(key).remove(data);
            }
        }, "pollFirst");
    }

    /**
     * 获取并移除队尾元素
     */
    public <V> V pollLast(String key) {
        return writeWithResult(() -> {
            RDeque<V> deque = primaryClient.getDeque(key);
            return deque.pollLast();
        }, data -> {
            if (data != null) {
                secondaryClient.getDeque(key).remove(data);
            }
        }, "pollLast");
    }

    /**
     * 批量添加元素到 Deque
     */
    public <V> Boolean dequeAddAll(String key, List<V> values) {
        return write(() -> {
            RDeque<V> deque = primaryClient.getDeque(key);
            return deque.addAll(values);
        }, () -> secondaryClient.getDeque(key).addAll(values), "dequeAddAll");
    }

    /**
     * 批量移除 Deque 元素
     */
    public <V> Boolean dequeRemoveAll(String key, List<V> values) {
        return write(() -> {
            RDeque<V> deque = primaryClient.getDeque(key);
            return deque.removeAll(values);
        }, () -> secondaryClient.getDeque(key).removeAll(values), "dequeRemoveAll");
    }

    // ====================== 阻塞队列 BlockingQueue ======================

    /**
     * 插入元素到阻塞队列
     */
    public <E> boolean offerBlockingQueue(String key, E value) {
        return write(() -> {
            RBlockingQueue<E> queue = primaryClient.getBlockingQueue(key);
            return queue.offer(value);
        }, () -> {
            RBlockingQueue<E> queue = secondaryClient.getBlockingQueue(key);
            queue.offer(value);
        }, "offerBlockingQueue");
    }

    /**
     * 从阻塞队列取出元素（阻塞）
     */
    public <E> E takeBlockingQueue(String key) throws InterruptedException {
        return writeWithResult(() -> {
            RBlockingQueue<E> queue = primaryClient.getBlockingQueue(key);
            try {
                return queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }, data -> {
            if (data != null) {
                RBlockingQueue<E> queue = secondaryClient.getBlockingQueue(key);
                queue.remove(data);
            }
        }, "takeBlockingQueue");
    }

    /**
     * 从阻塞队列取出元素（超时）
     */
    public <E> E pollBlockingQueue(String key, long timeout, TimeUnit unit) {
        return writeWithResult(() -> {
            RBlockingQueue<E> queue = primaryClient.getBlockingQueue(key);
            try {
                return queue.poll(timeout, unit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }, data -> {
            if (data != null) {
                RBlockingQueue<E> queue = secondaryClient.getBlockingQueue(key);
                queue.remove(data);
            }
        }, "pollBlockingQueue");
    }

    // ====================== Lock 操作 ======================

    /**
     * 获取可重入锁
     */
    public RLock getLock(String key) {
        return primaryClient.getLock(key);
    }

    /**
     * 尝试获取锁
     */
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = getLock(key);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 获取读写锁
     */
    public RReadWriteLock getReadWriteLock(String key) {
        return primaryClient.getReadWriteLock(key);
    }

    /**
     * 获取公平锁
     */
    public RLock getFairLock(String key) {
        return primaryClient.getFairLock(key);
    }

    // ====================== 地理位置 GEO ======================

    /**
     * 添加地理位置
     */
    public <V> long addGeoLocation(String key, double longitude, double latitude, V member) {
        return write(() -> {
            RGeo<V> geo = primaryClient.getGeo(key);
            return geo.add(longitude, latitude, member);
        }, () -> {
            RGeo<V> geo = secondaryClient.getGeo(key);
            geo.add(longitude, latitude, member);
        }, "addGeoLocation");
    }

    /**
     * 批量添加地理位置
     */
    public <V> long addGeoLocation(String key, GeoEntry... entries) {
        return write(() -> {
            RGeo<V> geo = primaryClient.getGeo(key);
            return geo.add(entries);
        }, () -> {
            RGeo<V> geo = secondaryClient.getGeo(key);
            geo.add(entries);
        }, "addGeoLocationBatch");
    }

    /**
     * 删除地理位置
     */
    public <V> boolean removeGeoLocation(String key, V value) {
        return write(() -> {
            RGeo<V> geo = primaryClient.getGeo(key);
            return geo.remove(value);
        }, () -> {
            RGeo<V> geo = secondaryClient.getGeo(key);
            geo.remove(value);
        }, "removeGeoLocation");
    }

    /**
     * 获取地理位置
     */
    @SafeVarargs
    public final <V> Map<V, GeoPosition> getGeoPosition(String key, V... members) {
        RGeo<V> geo = primaryClient.getGeo(key);
        return geo.pos(members);
    }

    /**
     * 计算两成员之间的距离
     */
    public <V> Double getGeoDistance(String key, V first, V second, GeoUnit unit) {
        return primaryClient.getGeo(key).dist(first, second, unit);
    }

    /**
     * 搜索指定位置附近的成员
     */
    public <V> List<V> searchGeo(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        RGeo<V> geo = primaryClient.getGeo(key);
        return geo.search(GeoSearchArgs.from(longitude, latitude).radius(radius, unit));
    }

    /**
     * 搜索指定位置附近的成员（带距离）
     */
    public <V> Map<V, Double> searchGeoWithDistance(String key, double longitude, double latitude,
                                                    double radius, GeoUnit unit, int count) {
        RGeo<V> geo = primaryClient.getGeo(key);
        return geo.searchWithDistance(GeoSearchArgs.from(longitude, latitude)
                .radius(radius, unit)
                .count(count)
                .order(GeoOrder.ASC));
    }

    // ====================== 布隆过滤器 BloomFilter ======================

    /**
     * 创建布隆过滤器
     */
    public boolean createBloomFilter(String key, long expectedInsertions, double falseProbability) {
        return write(() -> {
            RBloomFilter<Object> filter = primaryClient.getBloomFilter(key);
            return filter.tryInit(expectedInsertions, falseProbability);
        }, () -> {
            RBloomFilter<Object> filter = secondaryClient.getBloomFilter(key);
            filter.tryInit(expectedInsertions, falseProbability);
        }, "createBloomFilter");
    }

    /**
     * 添加元素到布隆过滤器
     */
    public <T> boolean addToBloomFilter(String key, T value) {
        return write(() -> {
            RBloomFilter<T> filter = primaryClient.getBloomFilter(key);
            return filter.add(value);
        }, () -> {
            RBloomFilter<T> filter = secondaryClient.getBloomFilter(key);
            filter.add(value);
        }, "addToBloomFilter");
    }

    /**
     * 检查元素是否可能存在于布隆过滤器中
     */
    public <T> boolean mightContainInBloomFilter(String key, T value) {
        RBloomFilter<T> filter = primaryClient.getBloomFilter(key);
        return filter.contains(value);
    }

    // ====================== 位图 BitSet ======================

    /**
     * 设置位图指定位置的值
     */
    public Boolean setBit(String key, long offset, boolean value) {
        return write(() -> {
            RBitSet bitSet = primaryClient.getBitSet(key);
            return bitSet.set(offset, value);
        }, () -> {
            RBitSet bitSet = secondaryClient.getBitSet(key);
            bitSet.set(offset, value);
        }, "setBit");
    }

    /**
     * 获取位图指定位置的值
     */
    public Boolean getBit(String key, long offset) {
        RBitSet bitSet = primaryClient.getBitSet(key);
        return bitSet.get(offset);
    }

    /**
     * 统计位图中值为 1 的数量
     */
    public Long bitCount(String key) {
        RBitSet bitSet = primaryClient.getBitSet(key);
        return bitSet.cardinality();
    }

    // ====================== HyperLogLog 操作 ======================

    /**
     * 添加元素到 HyperLogLog
     */
    public <T> Long pfadd(String key, T value) {
        return write(() -> {
            RHyperLogLog<T> hll = primaryClient.getHyperLogLog(key);
            hll.add(value);
            return hll.count();
        }, () -> {
            RHyperLogLog<T> hll = secondaryClient.getHyperLogLog(key);
            hll.add(value);
        }, "pfadd");
    }

    /**
     * 批量添加元素到 HyperLogLog
     */
    public <T> Long pfaddAll(String key, Collection<T> values) {
        return write(() -> {
            RHyperLogLog<T> hll = primaryClient.getHyperLogLog(key);
            hll.addAll(values);
            return hll.count();
        }, () -> {
            RHyperLogLog<T> hll = secondaryClient.getHyperLogLog(key);
            hll.addAll(values);
        }, "pfaddAll");
    }

    /**
     * 获取 HyperLogLog 的基数估计值
     */
    public Long pfcount(String key) {
        RHyperLogLog<?> hll = primaryClient.getHyperLogLog(key);
        return hll.count();
    }

    // ====================== 原子操作 ======================

    /**
     * 递增 1
     */
    public long increment(String key) {
        return increment(key, 1);
    }

    /**
     * 递增指定值
     */
    public long increment(String key, long delta) {
        return write(() -> {
            RAtomicLong atomicLong = primaryClient.getAtomicLong(key);
            return atomicLong.addAndGet(delta);
        }, () -> {
            RAtomicLong atomicLong = secondaryClient.getAtomicLong(key);
            atomicLong.addAndGet(delta);
        }, "increment");
    }

    /**
     * 递增 1 并设置过期时间
     */
    public long increment(String key, Duration duration) {
        return increment(key, 1, duration);
    }

    /**
     * 递增指定值并设置过期时间
     */
    public long increment(String key, long delta, Duration duration) {
        return write(() -> {
            RAtomicLong atomicLong = primaryClient.getAtomicLong(key);
            long result = atomicLong.addAndGet(delta);
            atomicLong.expire(duration);
            return result;
        }, () -> {
            RAtomicLong atomicLong = secondaryClient.getAtomicLong(key);
            atomicLong.addAndGet(delta);
            atomicLong.expire(duration);
        }, "incrementWithExpire");
    }

    /**
     * 递增 Double
     */
    public double incrementDouble(String key, double delta) {
        return write(() -> {
            RAtomicDouble atomicDouble = primaryClient.getAtomicDouble(key);
            return atomicDouble.addAndGet(delta);
        }, () -> {
            RAtomicDouble atomicDouble = secondaryClient.getAtomicDouble(key);
            atomicDouble.addAndGet(delta);
        }, "incrementDouble");
    }

    /**
     * 递增 Double 并设置过期时间
     */
    public double incrementDouble(String key, double delta, Duration duration) {
        return write(() -> {
            RAtomicDouble atomicDouble = primaryClient.getAtomicDouble(key);
            double result = atomicDouble.addAndGet(delta);
            atomicDouble.expire(duration);
            return result;
        }, () -> {
            RAtomicDouble atomicDouble = secondaryClient.getAtomicDouble(key);
            atomicDouble.addAndGet(delta);
            atomicDouble.expire(duration);
        }, "incrementDoubleWithExpire");
    }

    /**
     * 递减 1
     */
    public long decrement(String key) {
        return decrement(key, 1);
    }

    /**
     * 递减指定值
     */
    public long decrement(String key, long delta) {
        return write(() -> {
            RAtomicLong atomicLong = primaryClient.getAtomicLong(key);
            return atomicLong.addAndGet(-delta);
        }, () -> {
            RAtomicLong atomicLong = secondaryClient.getAtomicLong(key);
            atomicLong.addAndGet(-delta);
        }, "decrement");
    }

    /**
     * 递减 1 并设置过期时间
     */
    public long decrement(String key, Duration duration) {
        return decrement(key, 1, duration);
    }

    /**
     * 递减指定值并设置过期时间
     */
    public long decrement(String key, long delta, Duration duration) {
        return write(() -> {
            RAtomicLong atomicLong = primaryClient.getAtomicLong(key);
            long result = atomicLong.addAndGet(-delta);
            atomicLong.expire(duration);
            return result;
        }, () -> {
            RAtomicLong atomicLong = secondaryClient.getAtomicLong(key);
            atomicLong.addAndGet(-delta);
            atomicLong.expire(duration);
        }, "decrementWithExpire");
    }

    /**
     * 递减 Double
     */
    public double decrementDouble(String key, double delta) {
        return write(() -> {
            RAtomicDouble atomicDouble = primaryClient.getAtomicDouble(key);
            return atomicDouble.addAndGet(-delta);
        }, () -> {
            RAtomicDouble atomicDouble = secondaryClient.getAtomicDouble(key);
            atomicDouble.addAndGet(-delta);
        }, "decrementDouble");
    }

    /**
     * 递减 Double 并设置过期时间
     */
    public double decrementDouble(String key, double delta, Duration duration) {
        return write(() -> {
            RAtomicDouble atomicDouble = primaryClient.getAtomicDouble(key);
            double result = atomicDouble.addAndGet(-delta);
            atomicDouble.expire(duration);
            return result;
        }, () -> {
            RAtomicDouble atomicDouble = secondaryClient.getAtomicDouble(key);
            atomicDouble.addAndGet(-delta);
            atomicDouble.expire(duration);
        }, "decrementDoubleWithExpire");
    }

    /**
     * 获取原子 Long 值
     */
    public long getAtomicLong(String key) {
        return primaryClient.getAtomicLong(key).get();
    }

    /**
     * 设置原子 Long 值
     */
    public void setAtomicLong(String key, long value) {
        write(() -> {
            primaryClient.getAtomicLong(key).set(value);
            return null;
        }, () -> secondaryClient.getAtomicLong(key).set(value), "setAtomicLong");
    }

    /**
     * 设置原子 Long 值并设置过期时间
     */
    public void setAtomicLong(String key, long value, Duration duration) {
        write(() -> {
            RAtomicLong atomicLong = primaryClient.getAtomicLong(key);
            atomicLong.set(value);
            atomicLong.expire(duration);
            return null;
        }, () -> {
            RAtomicLong atomicLong = secondaryClient.getAtomicLong(key);
            atomicLong.set(value);
            atomicLong.expire(duration);
        }, "setAtomicLongWithExpire");
    }

    /**
     * 获取原子 Double 值
     */
    public double getAtomicDouble(String key) {
        return primaryClient.getAtomicDouble(key).get();
    }

    /**
     * 设置原子 Double 值
     */
    public void setAtomicDouble(String key, double value) {
        write(() -> {
            primaryClient.getAtomicDouble(key).set(value);
            return null;
        }, () -> secondaryClient.getAtomicDouble(key).set(value), "setAtomicDouble");
    }

    /**
     * 设置原子 Double 值并设置过期时间
     */
    public void setAtomicDouble(String key, double value, Duration duration) {
        write(() -> {
            RAtomicDouble atomicDouble = primaryClient.getAtomicDouble(key);
            atomicDouble.set(value);
            atomicDouble.expire(duration);
            return null;
        }, () -> {
            RAtomicDouble atomicDouble = secondaryClient.getAtomicDouble(key);
            atomicDouble.set(value);
            atomicDouble.expire(duration);
        }, "setAtomicDoubleWithExpire");
    }

    /**
     * 比较并设置原子 Long 值
     *
     * @param key    键
     * @param expect 期望值
     * @param update 更新值
     * @return 是否成功
     */
    public boolean compareAndSetLong(String key, long expect, long update) {
        boolean result = primaryClient.getAtomicLong(key).compareAndSet(expect, update);
        if (result && isDualWriteEnabled()) {
            asyncWrite(() -> secondaryClient.getAtomicLong(key).compareAndSet(expect, update), "compareAndSetLong");
        }
        return result;
    }

    /**
     * 比较并设置原子 Double 值
     *
     * @param key    键
     * @param expect 期望值
     * @param update 更新值
     * @return 是否成功
     */
    public boolean compareAndSetDouble(String key, double expect, double update) {
        boolean result = primaryClient.getAtomicDouble(key).compareAndSet(expect, update);
        if (result && isDualWriteEnabled()) {
            asyncWrite(() -> secondaryClient.getAtomicDouble(key).compareAndSet(expect, update), "compareAndSetDouble");
        }
        return result;
    }

    /**
     * 获取并设置原子 Long 值
     */
    public long getAndSetLong(String key, long value) {
        return writeWithResult(() -> primaryClient.getAtomicLong(key).getAndSet(value),
                result -> secondaryClient.getAtomicLong(key).set(value), "getAndSetLong");
    }

    /**
     * 获取并设置原子 Double 值
     */
    public double getAndSetDouble(String key, double value) {
        return writeWithResult(() -> primaryClient.getAtomicDouble(key).getAndSet(value),
                result -> secondaryClient.getAtomicDouble(key).set(value), "getAndSetDouble");
    }

    /**
     * 获取并递增原子 Long
     */
    public long getAndIncrement(String key) {
        return writeWithResult(() -> primaryClient.getAtomicLong(key).getAndIncrement(),
                result -> secondaryClient.getAtomicLong(key).incrementAndGet(), "getAndIncrement");
    }

    /**
     * 获取并递减原子 Long
     */
    public long getAndDecrement(String key) {
        return writeWithResult(() -> primaryClient.getAtomicLong(key).getAndDecrement(),
                result -> secondaryClient.getAtomicLong(key).decrementAndGet(), "getAndDecrement");
    }

    /**
     * 获取并增加原子 Long
     */
    public long getAndAdd(String key, long delta) {
        return writeWithResult(() -> primaryClient.getAtomicLong(key).getAndAdd(delta),
                result -> secondaryClient.getAtomicLong(key).addAndGet(delta), "getAndAdd");
    }

    /**
     * 获取并增加原子 Double
     */
    public double getAndAddDouble(String key, double delta) {
        return writeWithResult(() -> primaryClient.getAtomicDouble(key).getAndAdd(delta),
                result -> secondaryClient.getAtomicDouble(key).addAndGet(delta), "getAndAddDouble");
    }

    // ====================== 通用操作 ======================

    /**
     * 删除 key
     */
    public boolean delete(String key) {
        return write(() -> {
            primaryClient.getBucket(key).delete();
            return true;
        }, () -> secondaryClient.getBucket(key).delete(), "delete");
    }

    /**
     * 批量删除 key
     */
    public void delete(Collection<String> keys) {
        write(() -> {
            for (String key : keys) {
                primaryClient.getBucket(key).unlink();
            }
            return null;
        }, () -> {
            for (String key : keys) {
                secondaryClient.getBucket(key).unlink();
            }
        }, "deleteBatch");
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, Duration duration) {
        return write(() -> {
            primaryClient.getBucket(key).expire(duration);
            return true;
        }, () -> secondaryClient.getBucket(key).expire(duration), "expire");
    }

    /**
     * 判断 key 是否存在
     */
    public boolean exists(String key) {
        return primaryClient.getBucket(key).isExists();
    }

    /**
     * 获取 key 的剩余过期时间
     */
    public long getExpire(String key) {
        return primaryClient.getBucket(key).remainTimeToLive();
    }

    // ====================== 信号量操作 ======================

    /**
     * 初始化信号量许可数量
     *
     * @param key     信号量 key
     * @param permits 许可数量
     * @return 是否设置成功
     */
    public boolean trySetSemaphorePermits(String key, int permits) {
        return primaryClient.getSemaphore(key).trySetPermits(permits);
    }

    /**
     * 获取信号量可用许可数
     *
     * @param key 信号量 key
     * @return 可用许可数
     */
    public int getSemaphoreAvailablePermits(String key) {
        return primaryClient.getSemaphore(key).availablePermits();
    }

    /**
     * 尝试获取信号量许可（非阻塞）
     *
     * @param key 信号量 key
     * @return 是否获取成功
     */
    public boolean tryAcquireSemaphore(String key) {
        return primaryClient.getSemaphore(key).tryAcquire();
    }

    /**
     * 尝试获取信号量许可（带超时）
     *
     * @param key      信号量 key
     * @param waitTime 等待时间
     * @param unit     时间单位
     * @return 是否获取成功
     */
    public boolean tryAcquireSemaphore(String key, long waitTime, TimeUnit unit) throws InterruptedException {
        return primaryClient.getSemaphore(key).tryAcquire(waitTime, unit);
    }

    /**
     * 尝试获取多个信号量许可（带超时）
     *
     * @param key      信号量 key
     * @param permits  许可数量
     * @param waitTime 等待时间
     * @param unit     时间单位
     * @return 是否获取成功
     */
    public boolean tryAcquireSemaphore(String key, int permits, long waitTime, TimeUnit unit) throws InterruptedException {
        return primaryClient.getSemaphore(key).tryAcquire(permits, waitTime, unit);
    }

    /**
     * 释放信号量许可
     *
     * @param key 信号量 key
     */
    public void releaseSemaphore(String key) {
        primaryClient.getSemaphore(key).release();
    }

    /**
     * 释放多个信号量许可
     *
     * @param key     信号量 key
     * @param permits 许可数量
     */
    public void releaseSemaphore(String key, int permits) {
        primaryClient.getSemaphore(key).release(permits);
    }

    /**
     * 获取可过期信号量的许可（到期自动释放）
     *
     * @param key       信号量 key
     * @param waitTime  等待时间
     * @param leaseTime 租约时间（到期自动释放）
     * @param unit      时间单位
     * @return 许可ID（用于手动释放），获取失败返回 null
     */
    public String tryAcquireExpirableSemaphore(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        return primaryClient.getPermitExpirableSemaphore(key).tryAcquire(waitTime, leaseTime, unit);
    }

    /**
     * 初始化可过期信号量的许可数量
     *
     * @param key     信号量 key
     * @param permits 许可数量
     * @return 是否设置成功
     */
    public boolean trySetExpirableSemaphorePermits(String key, int permits) {
        return primaryClient.getPermitExpirableSemaphore(key).trySetPermits(permits);
    }

    /**
     * 释放可过期信号量的许可
     *
     * @param key      信号量 key
     * @param permitId 许可ID
     */
    public void releaseExpirableSemaphore(String key, String permitId) {
        primaryClient.getPermitExpirableSemaphore(key).release(permitId);
    }

    // ====================== 发布订阅操作 ======================

    /**
     * 发布消息到主题
     *
     * @param topic   主题名称
     * @param message 消息内容
     * @return 接收到消息的订阅者数量
     */
    public <T> long publish(String topic, T message) {
        return primaryClient.getTopic(topic).publish(message);
    }

    /**
     * 订阅主题
     *
     * @param topic    主题名称
     * @param type     消息类型
     * @param listener 消息监听器
     * @return 监听器ID（用于取消订阅）
     */
    public <T> int subscribe(String topic, Class<T> type, java.util.function.BiConsumer<CharSequence, T> listener) {
        return primaryClient.getTopic(topic).addListener(type, (channel, msg) -> listener.accept(channel, msg));
    }

    /**
     * 取消订阅
     *
     * @param topic      主题名称
     * @param listenerId 监听器ID
     */
    public void unsubscribe(String topic, int listenerId) {
        primaryClient.getTopic(topic).removeListener(listenerId);
    }

    /**
     * 获取主题的订阅者数量
     *
     * @param topic 主题名称
     * @return 订阅者数量
     */
    public int countSubscribers(String topic) {
        return primaryClient.getTopic(topic).countListeners();
    }

    // ====================== 限流器操作 ======================

    /**
     * 初始化限流器
     *
     * @param key      限流器 key
     * @param rate     速率
     * @param interval 时间间隔
     * @param unit     时间单位
     * @return 是否设置成功
     */
    public boolean trySetRateLimiter(String key, long rate, long interval, RateIntervalUnit unit) {
        return primaryClient.getRateLimiter(key).trySetRate(RateType.OVERALL, rate, interval, unit);
    }

    /**
     * 尝试获取限流器许可（非阻塞）
     *
     * @param key 限流器 key
     * @return 是否获取成功
     */
    public boolean tryAcquireRateLimiter(String key) {
        return primaryClient.getRateLimiter(key).tryAcquire();
    }

    /**
     * 尝试获取多个限流器许可（非阻塞）
     *
     * @param key     限流器 key
     * @param permits 许可数量
     * @return 是否获取成功
     */
    public boolean tryAcquireRateLimiter(String key, long permits) {
        return primaryClient.getRateLimiter(key).tryAcquire(permits);
    }

    /**
     * 获取限流器可用许可数
     *
     * @param key 限流器 key
     * @return 可用许可数
     */
    public long getRateLimiterAvailablePermits(String key) {
        return primaryClient.getRateLimiter(key).availablePermits();
    }

    /**
     * 检查限流器是否存在
     *
     * @param key 限流器 key
     * @return 是否存在
     */
    public boolean rateLimiterExists(String key) {
        return primaryClient.getRateLimiter(key).isExists();
    }

    /**
     * 删除限流器
     *
     * @param key 限流器 key
     * @return 是否删除成功
     */
    public boolean deleteRateLimiter(String key) {
        return primaryClient.getRateLimiter(key).delete();
    }

    // ====================== ZSet 扩展操作（延迟队列用） ======================

    /**
     * 获取指定分数范围内的元素数量
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素数量
     */
    public int zcount(String key, double min, double max) {
        return primaryClient.getScoredSortedSet(key).count(min, true, max, true);
    }

    /**
     * 移除 ZSet 中的元素
     *
     * @param key    键
     * @param member 元素
     * @return 是否移除成功
     */
    public <V> boolean zremove(String key, V member) {
        return write(() -> primaryClient.getScoredSortedSet(key).remove(member),
                () -> secondaryClient.getScoredSortedSet(key).remove(member),
                "zremove");
    }

    /**
     * 获取 ZSet 所有元素
     *
     * @param key 键
     * @return 元素集合
     */
    public <V> Collection<V> zgetAll(String key) {
        return primaryClient.<V>getScoredSortedSet(key).readAll();
    }

    /**
     * 清空 ZSet
     *
     * @param key 键
     * @return 是否成功
     */
    public boolean zclear(String key) {
        return write(() -> primaryClient.getScoredSortedSet(key).delete(),
                () -> secondaryClient.getScoredSortedSet(key).delete(),
                "zclear");
    }

    // ====================== 私有方法 ======================

    /**
     * 写操作包装方法，支持双写
     *
     * @param primaryAction   主集群操作
     * @param secondaryAction 备份集群操作
     * @param actionName      操作名称（用于日志）
     * @param <T>             返回值类型
     * @return 主集群操作结果
     */
    private <T> T write(Supplier<T> primaryAction, Runnable secondaryAction, String actionName) {
        T result = null;
        try {
            result = primaryAction.get();
            if (isDualWriteEnabled()) {
                asyncWrite(secondaryAction, actionName);
            }
        } catch (Exception e) {
            log.error("Redis {} operation failed on primary", actionName, e);
            throw e;
        }
        return result;
    }

    /**
     * 写操作包装方法，将主集群结果传递给备份集群
     *
     * @param primaryAction   主集群操作
     * @param secondaryAction 备份集群操作（接收主集群结果）
     * @param actionName      操作名称（用于日志）
     * @param <T>             返回值类型
     * @return 主集群操作结果
     */
    private <T> T writeWithResult(Supplier<T> primaryAction, Consumer<T> secondaryAction, String actionName) {
        T result = null;
        try {
            result = primaryAction.get();
            if (isDualWriteEnabled()) {
                T finalResult = result;
                asyncWrite(() -> secondaryAction.accept(finalResult), actionName);
            }
        } catch (Exception e) {
            log.error("Redis {} operation failed on primary", actionName, e);
            throw e;
        }
        return result;
    }

    /**
     * 异步执行备份集群写操作
     */
    private void asyncWrite(Runnable action, String actionName) {
        if (dualWriteExecutor == null) {
            log.warn("Dual write executor is null, skip secondary write for {}", actionName);
            return;
        }

        // 检查熔断器是否允许请求
        if (!circuitBreaker.allowRequest()) {
            log.debug("Circuit breaker is open, skip secondary write for {}", actionName);
            return;
        }

        try {
            if (metrics != null) {
                metrics.recordSubmit();
            }
            dualWriteExecutor.execute(() -> {
                try {
                    action.run();
                    if (metrics != null) {
                        metrics.recordSuccess(actionName);
                    }
                    // 记录熔断器成功
                    circuitBreaker.recordSuccess();
                } catch (Exception e) {
                    log.error("Redis {} operation failed on secondary", actionName, e);
                    if (metrics != null) {
                        metrics.recordFailure(actionName);
                    }
                    // 记录熔断器失败
                    circuitBreaker.recordFailure();
                }
            });
        } catch (Exception e) {
            log.error("Failed to submit {} task to dual write executor", actionName, e);
            if (metrics != null) {
                metrics.recordSubmitFailure();
            }
            // 提交失败也记录为熔断器失败
            circuitBreaker.recordFailure();
        }
    }
}
