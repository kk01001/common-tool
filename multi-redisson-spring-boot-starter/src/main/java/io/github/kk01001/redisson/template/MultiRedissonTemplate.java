package io.github.archer099.redisson.template;

import io.github.archer099.redisson.circuitbreaker.DualWriteCircuitBreaker;
import io.github.archer099.redisson.holder.RedissonClientHolder;
import io.github.archer099.redisson.monitor.DualWriteMetrics;
import io.github.archer099.redisson.properties.MultiRedissonProperties;
import io.github.archer099.redisson.retry.DualWriteFailureHandler;
import io.github.archer099.redisson.retry.RetryTask;
import io.github.archer099.redisson.retry.RetryableRunnable;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author archer099
 * @date 2026-01-15 14:00:00
 * @description Redisson 多集群操作模板，支持双写
 * <p>
 * 对外提供统一的 Redis 操作接口，内部自动处理双写逻辑：
 * - 主集群：同步操作
 * - 备份集群：异步操作（通过线程池）
 * </p>
 */
@SuppressWarnings("unchecked")
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
     * 双写失败处理器
     */
    private final DualWriteFailureHandler failureHandler;

    /**
     * 批量操作
     */
    private final BatchOperations batchOperations;

    public MultiRedissonTemplate(RedissonClientHolder holder,
                                 MultiRedissonProperties properties,
                                 ExecutorService dualWriteExecutor,
                                 DualWriteMetrics metrics,
                                 DualWriteCircuitBreaker circuitBreaker,
                                 DualWriteFailureHandler failureHandler) {
        this.properties = properties;
        this.primaryClient = holder.getPrimary();
        this.metrics = metrics;
        this.circuitBreaker = circuitBreaker;
        this.dualWriteExecutor = dualWriteExecutor;
        this.failureHandler = failureHandler;

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

        this.batchOperations = new BatchOperations(primaryClient, secondaryClient,
                dualWriteExecutor, properties, circuitBreaker, metrics, failureHandler);

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
     * <p>备份集群使用 set 而非 setIfAbsent，保证与主集群状态一致</p>
     */
    public <V> boolean setNx(String key, V value, Duration duration) {
        RBucket<V> bucket = primaryClient.getBucket(key);
        boolean result = bucket.setIfAbsent(value, duration);
        if (result && isDualWriteEnabled()) {
            asyncWrite(() -> secondaryClient.getBucket(key).set(value, duration), "setNx");
        }
        return result;
    }

    /**
     * 设置锁（如果不存在，指定编码器）
     * <p>备份集群使用 set 而非 setIfAbsent，保证与主集群状态一致</p>
     */
    public <V> boolean setNx(Codec codec, String key, V value, Duration duration) {
        RBucket<V> bucket = primaryClient.getBucket(key, codec);
        boolean result = bucket.setIfAbsent(value, duration);
        if (result && isDualWriteEnabled()) {
            asyncWrite(() -> secondaryClient.getBucket(key, codec).set(value, duration), "setNxWithCodec");
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
     * 判断 hash 字段是否存在
     */
    public boolean hexists(String key, String field) {
        RMap<String, ?> map = primaryClient.getMap(key);
        return map.containsKey(field);
    }

    /**
     * 获取 hash 所有字段名
     */
    public Set<String> hkeys(String key) {
        RMap<String, ?> map = primaryClient.getMap(key);
        return map.readAllKeySet();
    }

    /**
     * 获取 hash 所有值
     */
    public <V> Collection<V> hvals(String key) {
        RMap<String, V> map = primaryClient.getMap(key);
        return map.readAllValues();
    }

    /**
     * 设置 hash 字段（指定编码器）
     */
    public <V> void hset(Codec codec, String key, String field, V value) {
        write(() -> {
            RMap<String, V> map = primaryClient.getMap(key, codec);
            map.put(field, value);
            return null;
        }, () -> {
            RMap<String, V> map = secondaryClient.getMap(key, codec);
            map.put(field, value);
        }, "hsetWithCodec");
    }

    /**
     * 获取 hash 字段值（指定编码器）
     */
    public <V> V hget(Codec codec, String key, String field) {
        RMap<String, V> map = primaryClient.getMap(key, codec);
        return map.get(field);
    }

    /**
     * 获取所有 hash 字段和值（指定编码器）
     */
    public <K, V> Map<K, V> hgetAll(Codec codec, String key) {
        RMap<K, V> map = primaryClient.getMap(key, codec);
        return map.readAllMap();
    }

    /**
     * 批量设置 hash 字段（指定编码器）
     */
    public <V> void hmset(Codec codec, String key, Map<String, V> map) {
        write(() -> {
            RMap<String, V> rMap = primaryClient.getMap(key, codec);
            rMap.putAll(map);
            return null;
        }, () -> {
            RMap<String, V> rMap = secondaryClient.getMap(key, codec);
            rMap.putAll(map);
        }, "hmsetWithCodec");
    }

    /**
     * 仅当字段不存在时设置 hash 字段
     */
    public <V> V hsetNx(String key, String field, V value) {
        return write(() -> {
            RMap<String, V> map = primaryClient.getMap(key);
            return map.putIfAbsent(field, value);
        }, () -> {
            RMap<String, V> map = secondaryClient.getMap(key);
            map.put(field, value);
        }, "hsetNx");
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

    /**
     * 获取两个 Set 的交集
     */
    public <V> Set<V> sinter(String key, String otherKey) {
        RSet<V> set = primaryClient.getSet(key);
        return set.readIntersection(otherKey);
    }

    /**
     * 获取两个 Set 的并集
     */
    public <V> Set<V> sunion(String key, String otherKey) {
        RSet<V> set = primaryClient.getSet(key);
        return set.readUnion(otherKey);
    }

    /**
     * 获取两个 Set 的差集
     */
    public <V> Set<V> sdiff(String key, String otherKey) {
        RSet<V> set = primaryClient.getSet(key);
        return set.readDiff(otherKey);
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
     * 获取元素排名（从小到大）
     */
    public <V> Integer zrank(String key, V value) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.rank(value);
    }

    /**
     * 获取元素排名（从大到小）
     */
    public <V> Integer zrevrank(String key, V value) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.revRank(value);
    }

    /**
     * 按索引范围获取元素（从小到大）
     */
    public <V> Collection<V> zrange(String key, int start, int end) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.valueRange(start, end);
    }

    /**
     * 按索引范围获取元素（从大到小）
     */
    public <V> Collection<V> zrevrange(String key, int start, int end) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.valueRangeReversed(start, end);
    }

    /**
     * 按索引范围获取元素和分数（从小到大）
     */
    public <V> Collection<ScoredEntry<V>> zrangeWithScores(String key, int start, int end) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.entryRange(start, end);
    }

    /**
     * 按索引范围获取元素和分数（从大到小）
     */
    public <V> Collection<ScoredEntry<V>> zrevrangeWithScores(String key, int start, int end) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.entryRangeReversed(start, end);
    }

    /**
     * 按分数范围获取元素和分数
     */
    public <V> Collection<ScoredEntry<V>> zrangeByScoreWithScores(String key, double min, double max) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.entryRange(min, true, max, true);
    }

    /**
     * 按分数范围移除元素
     */
    public int zremrangeByScore(String key, double min, double max) {
        return write(() -> {
            RScoredSortedSet<?> zset = primaryClient.getScoredSortedSet(key);
            return zset.removeRangeByScore(min, true, max, true);
        }, () -> {
            RScoredSortedSet<?> zset = secondaryClient.getScoredSortedSet(key);
            zset.removeRangeByScore(min, true, max, true);
        }, "zremrangeByScore");
    }

    /**
     * 按排名范围移除元素
     */
    public int zremrangeByRank(String key, int start, int end) {
        return write(() -> {
            RScoredSortedSet<?> zset = primaryClient.getScoredSortedSet(key);
            return zset.removeRangeByRank(start, end);
        }, () -> {
            RScoredSortedSet<?> zset = secondaryClient.getScoredSortedSet(key);
            zset.removeRangeByRank(start, end);
        }, "zremrangeByRank");
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
     * <p>备份集群直接 set(update)，因为异步延迟导致备份集群当前值可能不等于 expect</p>
     *
     * @param key    键
     * @param expect 期望值
     * @param update 更新值
     * @return 是否成功
     */
    public boolean compareAndSetLong(String key, long expect, long update) {
        boolean result = primaryClient.getAtomicLong(key).compareAndSet(expect, update);
        if (result && isDualWriteEnabled()) {
            asyncWrite(() -> secondaryClient.getAtomicLong(key).set(update), "compareAndSetLong");
        }
        return result;
    }

    /**
     * 比较并设置原子 Double 值
     * <p>备份集群直接 set(update)，因为异步延迟导致备份集群当前值可能不等于 expect</p>
     *
     * @param key    键
     * @param expect 期望值
     * @param update 更新值
     * @return 是否成功
     */
    public boolean compareAndSetDouble(String key, double expect, double update) {
        boolean result = primaryClient.getAtomicDouble(key).compareAndSet(expect, update);
        if (result && isDualWriteEnabled()) {
            asyncWrite(() -> secondaryClient.getAtomicDouble(key).set(update), "compareAndSetDouble");
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
     * <p>备份集群直接 set(最终值)，避免增量操作累积误差</p>
     */
    public long getAndIncrement(String key) {
        return writeWithResult(() -> primaryClient.getAtomicLong(key).getAndIncrement(),
                oldValue -> secondaryClient.getAtomicLong(key).set(oldValue + 1), "getAndIncrement");
    }

    /**
     * 获取并递减原子 Long
     * <p>备份集群直接 set(最终值)，避免增量操作累积误差</p>
     */
    public long getAndDecrement(String key) {
        return writeWithResult(() -> primaryClient.getAtomicLong(key).getAndDecrement(),
                oldValue -> secondaryClient.getAtomicLong(key).set(oldValue - 1), "getAndDecrement");
    }

    /**
     * 获取并增加原子 Long
     * <p>备份集群直接 set(最终值)，避免增量操作累积误差</p>
     */
    public long getAndAdd(String key, long delta) {
        return writeWithResult(() -> primaryClient.getAtomicLong(key).getAndAdd(delta),
                oldValue -> secondaryClient.getAtomicLong(key).set(oldValue + delta), "getAndAdd");
    }

    /**
     * 获取并增加原子 Double
     * <p>备份集群直接 set(最终值)，避免增量操作累积误差</p>
     */
    public double getAndAddDouble(String key, double delta) {
        return writeWithResult(() -> primaryClient.getAtomicDouble(key).getAndAdd(delta),
                oldValue -> secondaryClient.getAtomicDouble(key).set(oldValue + delta), "getAndAddDouble");
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
     * 批量删除 key（可变参数）
     */
    public void delete(String... keys) {
        write(() -> {
            primaryClient.getKeys().unlink(keys);
            return null;
        }, () -> secondaryClient.getKeys().unlink(keys), "deleteBatch");
    }

    /**
     * 批量删除 key（集合）
     */
    public void delete(Collection<String> keys) {
        delete(keys.toArray(new String[0]));
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

    /**
     * 重命名 key
     */
    public void rename(String oldKey, String newKey) {
        write(() -> {
            primaryClient.getBucket(oldKey).rename(newKey);
            return null;
        }, () -> secondaryClient.getBucket(oldKey).rename(newKey), "rename");
    }

    /**
     * 移除 key 的过期时间（持久化）
     */
    public boolean persist(String key) {
        return write(() -> primaryClient.getBucket(key).clearExpire(),
                () -> secondaryClient.getBucket(key).clearExpire(), "persist");
    }

    /**
     * 按模式匹配查找 key（基于 SCAN 命令，不阻塞 Redis）
     *
     * @param pattern 匹配模式，如 "user:*"
     * @return 匹配的 key 迭代器
     */
    public Iterable<String> scan(String pattern) {
        return primaryClient.getKeys().getKeys(
                org.redisson.api.options.KeysScanOptions.defaults().pattern(pattern));
    }

    /**
     * 按模式匹配查找 key（指定每次扫描数量）
     *
     * @param pattern 匹配模式，如 "user:*"
     * @param count   每次 SCAN 返回的近似数量
     * @return 匹配的 key 迭代器
     */
    public Iterable<String> scan(String pattern, int count) {
        return primaryClient.getKeys().getKeys(
                org.redisson.api.options.KeysScanOptions.defaults().pattern(pattern).chunkSize(count));
    }

    /**
     * 按模式匹配查找 key
     *
     * @param pattern 匹配模式，如 "user:*"
     * @return 匹配的 key 集合
     */
    public Iterable<String> keys(String pattern) {
        return primaryClient.getKeys().getKeys(
                org.redisson.api.options.KeysScanOptions.defaults().pattern(pattern));
    }

    /**
     * 批量判断 key 是否存在
     *
     * @param keys key 数组
     * @return 存在的 key 数量
     */
    public long existsCount(String... keys) {
        return primaryClient.getKeys().countExists(keys);
    }

    // ====================== SCAN 系列操作 ======================

    /**
     * 遍历 Hash 字段（HSCAN）
     *
     * @param key     Hash 的 key
     * @param pattern 字段名匹配模式，如 "field:*"
     * @return 匹配的字段和值的迭代器
     */
    public <K, V> Iterable<Map.Entry<K, V>> hscan(String key, String pattern) {
        RMap<K, V> map = primaryClient.getMap(key);
        return map.entrySet(pattern);
    }

    /**
     * 遍历 Hash 字段（HSCAN，指定每次扫描数量）
     *
     * @param key     Hash 的 key
     * @param pattern 字段名匹配模式
     * @param count   每次 SCAN 返回的近似数量
     * @return 匹配的字段和值的迭代器
     */
    public <K, V> Iterable<Map.Entry<K, V>> hscan(String key, String pattern, int count) {
        RMap<K, V> map = primaryClient.getMap(key);
        return map.entrySet(pattern, count);
    }

    /**
     * 遍历 Set 元素（SSCAN）
     *
     * @param key     Set 的 key
     * @param pattern 元素匹配模式
     * @return 匹配的元素迭代器
     */
    public <V> Iterator<V> sscan(String key, String pattern) {
        RSet<V> set = primaryClient.getSet(key);
        return set.iterator(pattern);
    }

    /**
     * 遍历 Set 元素（SSCAN，指定每次扫描数量）
     *
     * @param key     Set 的 key
     * @param pattern 元素匹配模式
     * @param count   每次 SCAN 返回的近似数量
     * @return 匹配的元素迭代器
     */
    public <V> Iterator<V> sscan(String key, String pattern, int count) {
        RSet<V> set = primaryClient.getSet(key);
        return set.iterator(pattern, count);
    }

    /**
     * 遍历 ZSet 元素（ZSCAN）
     *
     * @param key     ZSet 的 key
     * @param pattern 元素匹配模式
     * @return 匹配的元素迭代器
     */
    public <V> Iterator<V> zscan(String key, String pattern) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.iterator(pattern);
    }

    /**
     * 遍历 ZSet 元素（ZSCAN，指定每次扫描数量）
     *
     * @param key     ZSet 的 key
     * @param pattern 元素匹配模式
     * @param count   每次 SCAN 返回的近似数量
     * @return 匹配的元素迭代器
     */
    public <V> Iterator<V> zscan(String key, String pattern, int count) {
        RScoredSortedSet<V> zset = primaryClient.getScoredSortedSet(key);
        return zset.iterator(pattern, count);
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
            if (failureHandler != null) {
                failureHandler.onCircuitBreakerSkip(new RetryTask(action, actionName));
            }
            return;
        }

        try {
            if (metrics != null) {
                metrics.recordSubmit();
            }
            dualWriteExecutor.execute(new RetryableRunnable(() -> {
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
                    if (failureHandler != null) {
                        failureHandler.onWriteFailure(new RetryTask(action, actionName));
                    }
                }
            }, actionName));
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
