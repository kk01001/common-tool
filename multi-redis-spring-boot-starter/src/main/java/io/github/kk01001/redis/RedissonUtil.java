package io.github.kk01001.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.redisson.api.GeoEntry;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoPosition;
import org.redisson.api.GeoUnit;
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
import org.redisson.api.RedissonClient;
import org.redisson.api.geo.GeoSearchArgs;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.ScoredEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description
 * Redisson工具类，支持多机房Redis操作，包含string、hash、set、list、deque、zset、lock等操作
 */
@Component
public class RedissonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonUtil.class);

    private static final Map<String, RedissonClient> REDISSON_CLIENT_MAP = new ConcurrentHashMap<>(16);

    @Qualifier(value = "redissonClient")
    @Autowired
    private RedissonClient redissonClient;

    @Qualifier(value = "redissonClient2")
    @Autowired(required = false)
    private RedissonClient redissonClient2;

    @Resource
    private MultiRedisProperties redisProperties;

    @Resource(name = "otherRoomExecutor")
    private ExecutorService otherExecutor;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 初始化客户端
     */
    @PostConstruct
    public void initClient() {
        String location = redisProperties.getCluster().getLocation();
        REDISSON_CLIENT_MAP.put(location, redissonClient);
        if (Optional.ofNullable(redissonClient2).isPresent()) {
            String location2 = redisProperties.getCluster2().getLocation();
            REDISSON_CLIENT_MAP.put(location2, redissonClient2);
        }
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    public RedissonClient getBackRedissonClient() {
        return redissonClient2;
    }

    /**
     * 根据机房位置获取RedissonClient
     *
     * @param location 机房位置
     * @return RedissonClient
     */
    public RedissonClient getRedissonClient(String location) {
        return REDISSON_CLIENT_MAP.get(location);
    }

    // ====================== String 操作 ======================
    /**
     * 设置字符串值
     */
    public <V> Boolean set(String key, V value) {
        return write(() -> {
            redissonClient.getBucket(key).set(value);
            return true;
        }, () -> redissonClient2.getBucket(key).set(value), "setBucket");
    }

    /**
     * 设置字符串值和过期时间
     */
    public <V> Boolean set(String key, V value, Duration duration) {
        return write(() -> {
            redissonClient.getBucket(key).set(value, duration);
            return true;
        }, () -> redissonClient2.getBucket(key).set(value, duration), "setBucketExpire");
    }

    /**
     * 序列化对象为JSON并存储
     */
    @SneakyThrows
    public Boolean setSerialize(String key, Object value) {
        String json = objectMapper.writeValueAsString(value);
        return write(() -> {
            redissonClient.getBucket(key).set(json);
            return true;
        }, () -> redissonClient2.getBucket(key).set(json), "setSerialize");
    }

    /**
     * 设置锁 过期时间单位 分钟
     */
    public <V> boolean setNx(String key, V value, Duration duration) {
        RBucket<V> bucket = redissonClient.getBucket(key);
        return bucket.setIfAbsent(value, duration);
    }

    /**
     * 获取字符串值
     */
    public <V> V get(String key) {
        RBucket<V> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 获取并反序列化为指定类型对象
     */
    public <T> T get(String key, Class<T> clazz) throws Exception {
        RBucket<String> bucket = redissonClient.getBucket(key);
        String data = bucket.get();
        if (!StringUtils.hasLength(data)) {
            return null;
        }
        return objectMapper.readValue(data, clazz);
    }

    /**
     * 使用指定编码器获取值
     */
    public <V> V get(Codec codec, String key) {
        RBucket<V> bucket = redissonClient.getBucket(key, codec);
        return bucket.get();
    }

    // ====================== Hash 操作 ======================
    /**
     * 设置hash字段
     */
    public <V> void hset(String key, String field, V value) {
        write(() -> {
            RMap<String, V> map = redissonClient.getMap(key);
            map.put(field, value);
            return null;
        }, () -> {
            RMap<String, V> map = redissonClient2.getMap(key);
            map.put(field, value);
        }, "hashSet");
    }

    /**
     * 批量设置hash字段
     */
    public <V> void hmset(String key, Map<String, V> map) {
        write(() -> {
            RMap<String, V> rMap = redissonClient.getMap(key);
            rMap.putAll(map);
            return null;
        }, () -> {
            RMap<String, V> rMap = redissonClient2.getMap(key);
            rMap.putAll(map);
        }, "hashMSet");
    }

    /**
     * 获取hash字段值
     */
    public <V> int hgetCount(String key) {
        RMap<String, V> map = redissonClient.getMap(key);
        return map.size();
    }

    /**
     * 获取hash字段值
     */
    public <V> V hget(String key, String field) {
        RMap<String, V> map = redissonClient.getMap(key);
        return map.get(field);
    }

    /**
     * 获取整个hash
     *
     * @param key 键
     * @param clazz 对象class类型
     * @param <V> 对象泛型
     * @return 对象
     */
    public <V> V hget(String key, Class<V> clazz) {
        Map<Object, Object> allMap = redissonClient.getMap(key).readAllMap();
        return objectMapper.convertValue(allMap, clazz);
    }

    /**
     * 获取所有hash字段和值
     */
    public <K, V> Map<K, V> hgetAll(String key) {
        RMap<K, V> map = redissonClient.getMap(key);
        return map.readAllMap();
    }

    /**
     * 删除hash字段
     */
    public Long hdel(String key, String... fields) {
        return write(() -> {
            RMap<String, ?> map = redissonClient.getMap(key);
            return map.fastRemove(fields);
        }, () -> {
            RMap<String, ?> map = redissonClient2.getMap(key);
            map.fastRemove(fields);
        }, "hashDel");
    }

    /**
     * hash元素递增
     */
    public <V> V hincrby(String key, String field, Long value) {
        return write(() -> {
                    RMap<Object, V> rMap = redissonClient.getMap(key);
                    return rMap.addAndGet(field, value);
                },
                () -> redissonClient2.getMap(key).addAndGet(field, value),
                "hashDel");
    }

    // ====================== Set 操作 ======================
    /**
     * 添加Set元素（支持可变参数）
     */
    public <V> Boolean sadd(String key, V... values) {
        return write(() -> {
            RSet<V> set = redissonClient.getSet(key);
            return set.addAll(Arrays.asList(values));
        }, () -> {
            RSet<V> set = redissonClient2.getSet(key);
            set.addAll(Arrays.asList(values));
        }, "setAdd");
    }

    /**
     * 添加Set元素（支持List参数）
     */
    public <V> Boolean saddAll(String key, List<V> values) {
        return write(() -> {
            RSet<V> set = redissonClient.getSet(key);
            return set.addAll(values);
        }, () -> {
            RSet<V> set = redissonClient2.getSet(key);
            set.addAll(values);
        }, "setAddList");
    }

    /**
     * 添加Set元素（支持Set参数）
     */
    public <V> Boolean saddAll(String key, Set<V> values) {
        return write(() -> {
            RSet<V> set = redissonClient.getSet(key);
            return set.addAll(values);
        }, () -> {
            RSet<V> set = redissonClient2.getSet(key);
            set.addAll(values);
        }, "setAddSet");
    }

    /**
     * 移除Set元素（支持可变参数）
     */
    public <V> Boolean srem(String key, V... values) {
        return write(() -> {
            RSet<V> set = redissonClient.getSet(key);
            return set.removeAll(Arrays.asList(values));
        }, () -> {
            RSet<V> set = redissonClient2.getSet(key);
            set.removeAll(Arrays.asList(values));
        }, "setRemove");
    }

    /**
     * 移除Set元素（支持List参数）
     */
    public <V> Boolean sremAll(String key, List<V> values) {
        return write(() -> {
            RSet<V> set = redissonClient.getSet(key);
            return set.removeAll(values);
        }, () -> {
            RSet<V> set = redissonClient2.getSet(key);
            set.removeAll(values);
        }, "setRemoveList");
    }

    /**
     * 移除Set元素（支持Set参数）
     */
    public <V> Boolean sremAll(String key, Set<V> values) {
        return write(() -> {
            RSet<V> set = redissonClient.getSet(key);
            return set.removeAll(values);
        }, () -> {
            RSet<V> set = redissonClient2.getSet(key);
            set.removeAll(values);
        }, "setRemoveSet");
    }

    /**
     * 获取Set所有元素
     */
    public <V> Set<V> smembers(String key) {
        RSet<V> set = redissonClient.getSet(key);
        return set.readAll();
    }

    /**
     * 判断元素是否在Set中
     */
    public <V> Boolean sismember(String key, V value) {
        RSet<V> set = redissonClient.getSet(key);
        return set.contains(value);
    }

    /**
     * 随机取出一个元素
     */
    public <V> V sRandom(String key, V value) {
        RSet<V> set = redissonClient.getSet(key);
        return set.random();
    }

    /**
     * 随机移除Set中的一个元素
     */
    public <V> V spop(String key) {
        return writeWithResult(() -> {
            RSet<V> set = redissonClient.getSet(key);
            return set.removeRandom();
        }, data -> {
            RSet<V> set = redissonClient2.getSet(key);
            set.remove(data);
        }, "setPopRandom");
    }

    /**
     * 随机移除Set中的多个元素
     */
    public <V> Set<V> spop(String key, int count) {
        return writeWithResult(() -> {
            RSet<V> set = redissonClient.getSet(key);
            return set.removeRandom(count);
        }, data -> {
            RSet<V> set = redissonClient2.getSet(key);
            set.removeAll(data);
        }, "setPopRandomCount");
    }

    // ====================== List 操作 ======================
    /**
     * 添加List元素
     */
    public <V> Boolean lpush(String key, V... values) {
        return write(() -> {
            RList<V> list = redissonClient.getList(key);
            return list.addAll(Arrays.asList(values));
        }, () -> {
            RList<V> list = redissonClient2.getList(key);
            list.addAll(Arrays.asList(values));
        }, "listPush");
    }

    /**
     * 查询全部List
     *
     * @param key 键
     * @return 如果列表包含指定元素，则返回true
     */
    public <V> List<V> lrange(String key) {
        RList<V> list = redissonClient.getList(key);
        return list.readAll();
    }

    /**
     * 获取List指定范围的元素
     */
    public <V> List<V> lrange(String key, int start, int end) {
        RList<V> list = redissonClient.getList(key);
        return list.range(start, end);
    }

    /**
     * 移除List中的元素
     */
    public <V> Boolean lrem(String key, V value) {
        return write(() -> {
            RList<V> list = redissonClient.getList(key);
            return list.remove(value);
        }, () -> {
            RList<V> list = redissonClient2.getList(key);
            list.remove(value);
        }, "listRemove");
    }

    /**
     * 批量添加元素到列表
     */
    public <V> Boolean lpushAll(String key, List<V> values) {
        return write(() -> {
            RList<V> list = redissonClient.getList(key);
            return list.addAll(values);
        }, () -> {
            RList<V> list = redissonClient2.getList(key);
            list.addAll(values);
        }, "listPushAll");
    }

    /**
     * 批量移除列表中的元素
     */
    public <V> Boolean lremAll(String key, List<V> values) {
        return write(() -> {
            RList<V> list = redissonClient.getList(key);
            return list.removeAll(values);
        }, () -> {
            RList<V> list = redissonClient2.getList(key);
            list.removeAll(values);
        }, "listRemoveAll");
    }

    /**
     * 检查列表中是否包含指定元素
     *
     * @param key 键
     * @param value 要检查的元素
     * @return 如果列表包含指定元素，则返回true
     */
    public <V> Boolean lcontains(String key, V value) {
        RList<V> list = redissonClient.getList(key);
        return list.contains(value);
    }

    // ====================== ZSet 操作 ======================
    /**
     * 添加有序集合元素
     */
    public <V> Boolean zadd(String key, V value, double score) {
        return write(() -> {
            RScoredSortedSet<V> zset = redissonClient.getScoredSortedSet(key);
            return zset.add(score, value);
        }, () -> {
            RScoredSortedSet<V> zset = redissonClient2.getScoredSortedSet(key);
            zset.add(score, value);
        }, "zsetAdd");
    }

    /**
     * 获取有序集合指定分数范围的元素
     */
    public <V> Collection<V> zrangeByScore(String key, double min, double max) {
        RScoredSortedSet<V> zset = redissonClient.getScoredSortedSet(key);
        return zset.valueRange(min, true, max, true);
    }

    /**
     * 获取元素的分数
     */
    public <V> Double zscore(String key, V value) {
        RScoredSortedSet<V> zset = redissonClient.getScoredSortedSet(key);
        return zset.getScore(value);
    }

    /**
     * 批量添加有序集合元素
     *
     * @param key 键
     * @param values 元素和分数的映射
     * @return 成功添加的元素个数
     */
    public <V> int zaddAll(String key, Map<V, Double> values) {
        return write(() -> {
            RScoredSortedSet<V> zset = redissonClient.getScoredSortedSet(key);
            return zset.addAll(values);
        }, () -> {
            RScoredSortedSet<V> zset = redissonClient2.getScoredSortedSet(key);
            zset.addAll(values);
        }, "zsetAddBatch");
    }

    /**
     * 获取有序集合中指定元素的排名（从大到小）
     *
     * @param key 键
     * @param value 元素
     * @return 排名，从0开始；如果元素不存在，返回null
     */
    public <V> Integer zrevrank(String key, V value) {
        RScoredSortedSet<V> zset = redissonClient.getScoredSortedSet(key);
        return zset.revRank(value);
    }

    /**
     * 按分数从大到小返回元素
     *
     * @param key 键
     * @param start 开始位置（包含）
     * @param end 结束位置（包含）
     * @return 元素集合
     */
    public <V> Collection<V> zrevrange(String key, int start, int end) {
        RScoredSortedSet<V> zset = redissonClient.getScoredSortedSet(key);
        return zset.valueRangeReversed(start, end);
    }

    /**
     * 获取有序集合中所有元素和分数
     *
     * @param key 键
     * @return 元素和分数的映射
     */
    public <V> Collection<ScoredEntry<V>> zgetAllWithScores(String key) {
        RScoredSortedSet<V> zset = redissonClient.getScoredSortedSet(key);
        return zset.entryRange(0, -1);
    }

    /**
     * 移除有序集合中的一个或多个元素
     *
     * @param key 键
     * @param values 要移除的元素
     * @return 成功移除的元素个数
     */
    public <V> boolean zrem(String key, V... values) {
        return write(() -> {
            RScoredSortedSet<V> zset = redissonClient.getScoredSortedSet(key);
            return zset.removeAll(Arrays.asList(values));
        }, () -> {
            RScoredSortedSet<V> zset = redissonClient2.getScoredSortedSet(key);
            zset.removeAll(Arrays.asList(values));
        }, "zsetRemove");
    }

    /**
     * 获取有序集合的大小
     *
     * @param key 键
     * @return 有序集合中的元素个数
     */
    public int zcard(String key) {
        RScoredSortedSet<?> zset = redissonClient.getScoredSortedSet(key);
        return zset.size();
    }

    /**
     * 获取有序集合中指定分数范围内的元素个数
     *
     * @param key 键
     * @param min 最小分数（包含）
     * @param max 最大分数（包含）
     * @return 分数范围内的元素个数
     */
    public int zcount(String key, double min, double max) {
        RScoredSortedSet<?> zset = redissonClient.getScoredSortedSet(key);
        return zset.count(min, true, max, true);
    }

    /**
     * 增加有序集合中元素的分数
     *
     * @param key 键
     * @param value 元素
     * @param delta 增加的分数
     * @return 增加后的分数
     */
    public <V> Double zincrby(String key, V value, double delta) {
        return write(() -> {
            RScoredSortedSet<V> zset = redissonClient.getScoredSortedSet(key);
            return zset.addScore(value, delta);
        }, () -> {
            RScoredSortedSet<V> zset = redissonClient2.getScoredSortedSet(key);
            zset.addScore(value, delta);
        }, "zsetIncrementScore");
    }

    // ====================== Deque 操作 ======================
    /**
     * 添加双端队列元素到队尾
     */
    public <V> Boolean offerLast(String key, V value) {
        return write(() -> redissonClient.getDeque(key).offerLast(value),
                () -> redissonClient2.getDeque(key).offerLast(value),
                "dequePushLast"
        );
    }

    /**
     * 添加双端队列元素到队首
     */
    public <V> Boolean offerFirst(String key, V value) {
        return write(() -> redissonClient.getDeque(key).offerFirst(value),
                () -> redissonClient2.getDeque(key).offerFirst(value),
                "dequePushFirst"
        );
    }

    /**
     * 获取并移除队首元素
     */
    public <V> V pollFirst(String key) {
        return writeWithResult(() -> {
                    RDeque<V> rDeque = redissonClient.getDeque(key);
                    return rDeque.pollFirst();
                },
                data -> redissonClient2.getDeque(key).remove(data),
                "dequePollFirst"
        );
    }

    /**
     * 获取并移除队尾元素
     */
    public <V> V pollLast(String key) {
        return writeWithResult(() -> {
                    RDeque<V> rDeque = redissonClient.getDeque(key);
                    return rDeque.pollLast();
                },
                data -> redissonClient2.getDeque(key).remove(data),
                "dequePollLast"
        );
    }

    /**
     * deque 添加List
     */
    public <V> Boolean dequeAddAll(String key, List<V> values) {
        return write(() -> {
                    RDeque<V> rDeque = redissonClient.getDeque(key);
                    return rDeque.addAll(values);
                },
                () -> redissonClient2.getDeque(key).addAll(values),
                "dequeAddAll"
        );
    }

    /**
     * deque 移除List
     */
    public <V> Boolean dequeRemoveAll(String key, List<V> values) {
        return write(() -> {
                    RDeque<V> rDeque = redissonClient.getDeque(key);
                    return rDeque.removeAll(values);
                },
                () -> redissonClient2.getDeque(key).removeAll(values),
                "dequeRemoveAll"
        );
    }

    /**
     * 是否存在
     */
    public <V> boolean containDeque(String key, V value) {
        return redissonClient.getDeque(key).contains(value);
    }

    // ====================== Lock 操作 ======================
    /**
     * 获取可重入锁
     */
    public RLock getLock(String key) {
        return redissonClient.getLock(key);
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
        return redissonClient.getReadWriteLock(key);
    }

    /**
     * 获取公平锁
     */
    public RLock getFairLock(String key) {
        return redissonClient.getFairLock(key);
    }

    // ====================== 阻塞队列 BlockingQueue ======================
    /**
     * 如果可能，将指定的元素插入到此队列中 所以立即在不违反容量限制的情况下，返回 成功时为 {@code true}，如果当前没有空格，则为
     * {@code false} 可用。检索并删除此队列的头部，必要时等待 ，直到某个元素变为可用。
     */
    public <E> boolean offerBlockingQueue(String key, E value) {
        return writeWithResult(() -> {
            RBlockingQueue<E> blockingQueue = redissonClient.getBlockingQueue(key);
            return blockingQueue.offer(value);
        }, data -> {
            RBlockingQueue<E> blockingQueue = redissonClient2.getBlockingQueue(key);
            blockingQueue.offer(value);
        }, "offerBlockingQueue");
    }

    /**
     * 检索并删除此队列的头部，必要时等待 ，直到某个元素变为可用。
     */
    public <E> E takeBlockingQueue(String key) {
        return writeWithResult(() -> {
            RBlockingQueue<E> blockingQueue = redissonClient.getBlockingQueue(key);
            try {
                return blockingQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, data -> {
            RBlockingQueue<E> blockingQueue = redissonClient2.getBlockingQueue(key);
            blockingQueue.remove(data);
        }, "takeBlockingQueue");
    }

    /**
     * 检索并删除此队列的头部，等待 指定等待时间（如有必要）以使元素变为可用。
     */
    public <E> E pollBlockingQueue(String key, long timeout, TimeUnit unit) {
        return writeWithResult(() -> {
            RBlockingQueue<E> blockingQueue = redissonClient.getBlockingQueue(key);
            try {
                return blockingQueue.poll(timeout, unit);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, data -> {
            RBlockingQueue<E> blockingQueue = redissonClient2.getBlockingQueue(key);
            blockingQueue.remove(data);
        }, "pollBlockingQueue");
    }

    // ====================== 地理位置 GEO ======================
    /**
     * 添加地理位置
     */
    public <V> long addGeoLocation(String key, double longitude, double latitude, V member) {
        return write(() -> {
            RGeo<V> geo = redissonClient.getGeo(key);
            return geo.add(longitude, latitude, member);
        }, () -> {
            RGeo<V> geo = redissonClient2.getGeo(key);
            geo.add(longitude, latitude, member);
        }, "addGeoLocation");
    }

    /**
     * 添加地理位置 GeoEntry
     */
    public <V> long addGeoLocation(String key, GeoEntry... geoEntry) {
        return write(() -> {
            RGeo<V> geo = redissonClient.getGeo(key);
            return geo.add(geoEntry);
        }, () -> {
            RGeo<V> geo = redissonClient2.getGeo(key);
            geo.add(geoEntry);
        }, "addGeoLocation");
    }

    /**
     * 删除地理位置
     */
    public <V> boolean removeGeoLocation(String key, V value) {
        return write(() -> {
            RGeo<V> geo = redissonClient.getGeo(key);
            return geo.remove(value);
        }, () -> {
            RGeo<V> geo = redissonClient2.getGeo(key);
            geo.remove(value);
        }, "removeGeoLocation");
    }

    /**
     * 批量删除地理位置
     */
    public <V> boolean removeGeoLocations(String key, List<V> values) {
        return write(() -> {
            RGeo<V> geo = redissonClient.getGeo(key);
            return geo.removeAll(values);
        }, () -> {
            RGeo<V> geo = redissonClient2.getGeo(key);
            geo.removeAll(values);
        }, "removeGeoLocations");
    }

    /**
     * 获取地理位置
     */
    @SafeVarargs
    public final <V> Map<V, GeoPosition> getGeoPosition(String key, V... members) {
        RGeo<V> geo = redissonClient.getGeo(key);
        return geo.pos(members);
    }

    /**
     * 计算两成员之间的距离
     */
    public <V> Double getDistance(String key, V firstMember, V secondMember, GeoUnit geoUnit) {
        return redissonClient.getGeo(key).dist(firstMember, secondMember, geoUnit);
    }

    /**
     * 获取指定位置, 返回排序集的成员，这些成员位于指定搜索条件的边框。
     */
    public <V> List<V> searchGeo(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        RGeo<V> geo = redissonClient.getGeo(key);
        return geo.search(GeoSearchArgs.from(longitude, latitude)
                .radius(radius, unit));
    }

    /**
     * 获取指定位置, 返回按排序集的成员映射的距离，位于指定搜索条件的边界内。
     */
    public <V> Map<V, Double> searchGeoWithDistance(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        RGeo<V> geo = redissonClient.getGeo(key);
        return geo.searchWithDistance(GeoSearchArgs.from(longitude, latitude)
                .radius(radius, unit)
                .order(GeoOrder.ASC));
    }

    /**
     * 获取指定位置, 返回按排序集的成员映射的位置，位于指定搜索条件的边界内。
     */
    public <V> Map<V, GeoPosition> searchGeoWithPosition(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        RGeo<V> geo = redissonClient.getGeo(key);
        return geo.searchWithPosition(GeoSearchArgs.from(longitude, latitude)
                .radius(radius, unit)
                .order(GeoOrder.ASC));
    }

    // ====================== 布隆过滤器 BloomFilter ======================

    /**
     * 创建布隆过滤器
     *
     * @param key                布隆过滤器的key
     * @param expectedInsertions 预期插入的元素数量
     * @param falseProbability   期望的误判率，如0.01表示1%
     * @return 是否创建成功
     */
    public boolean createBloomFilter(String key, long expectedInsertions, double falseProbability) {
        return write(() -> {
            RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(key);
            bloomFilter.tryInit(expectedInsertions, falseProbability);
            return true;
        }, () -> {
            RBloomFilter<Object> bloomFilter = redissonClient2.getBloomFilter(key);
            bloomFilter.tryInit(expectedInsertions, falseProbability);
        }, "createBloomFilter");
    }

    /**
     * 添加元素到布隆过滤器
     *
     * @param key   布隆过滤器的key
     * @param value 要添加的元素
     * @return 是否添加成功
     */
    public <T> boolean addToBloomFilter(String key, T value) {
        return write(() -> {
            RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(key);
            return bloomFilter.add(value);
        }, () -> {
            RBloomFilter<T> bloomFilter = redissonClient2.getBloomFilter(key);
            bloomFilter.add(value);
        }, "addToBloomFilter");
    }

    /**
     * 检查元素是否可能存在于布隆过滤器中
     *
     * @param key   布隆过滤器的key
     * @param value 要检查的元素
     * @return 如果元素可能存在返回true，如果元素一定不存在返回false
     */
    public <T> boolean mightContainInBloomFilter(String key, T value) {
        RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(key);
        return bloomFilter.contains(value);
    }

    /**
     * 获取布隆过滤器中已添加的元素数量
     *
     * @param key 布隆过滤器的key
     * @return 已添加的元素数量
     */
    public long getBloomFilterCount(String key) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(key);
        return bloomFilter.count();
    }

    /**
     * 获取布隆过滤器的容量
     *
     * @param key 布隆过滤器的key
     * @return 布隆过滤器的容量
     */
    public long getBloomFilterSize(String key) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(key);
        return bloomFilter.getSize();
    }

    /**
     * 获取布隆过滤器的误判率
     *
     * @param key 布隆过滤器的key
     * @return 误判率
     */
    public double getBloomFilterFalseProbability(String key) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(key);
        return bloomFilter.getFalseProbability();
    }

    /**
     * 删除布隆过滤器
     *
     * @param key 布隆过滤器的key
     * @return 是否删除成功
     */
    public boolean deleteBloomFilter(String key) {
        return write(() -> {
            RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(key);
            return bloomFilter.unlink();
        }, () -> {
            RBloomFilter<Object> bloomFilter = redissonClient2.getBloomFilter(key);
            bloomFilter.unlink();
        }, "deleteBloomFilter");
    }

    // ====================== 位图 bitmap ======================

    /**
     * 设置位图指定位置的值
     *
     * @param key    键
     * @param offset 偏移量
     * @param value  true 为 1，false 为 0
     * @return 该位置原来的值
     */
    public Boolean setBit(String key, long offset, boolean value) {
        return write(() -> {
            RBitSet bitSet = redissonClient.getBitSet(key);
            return bitSet.set(offset, value);
        }, () -> {
            RBitSet bitSet = redissonClient2.getBitSet(key);
            bitSet.set(offset, value);
        }, "setBit");
    }

    /**
     * 获取位图指定位置的值
     *
     * @param key    键
     * @param offset 偏移量
     * @return true 为 1，false 为 0
     */
    public Boolean getBit(String key, long offset) {
        RBitSet bitSet = redissonClient.getBitSet(key);
        return bitSet.get(offset);
    }

    /**
     * 统计位图中值为 1 的数量
     *
     * @param key 键
     * @return 值为 1 的数量
     */
    public Long bitCount(String key) {
        RBitSet bitSet = redissonClient.getBitSet(key);
        return bitSet.cardinality();
    }

    /**
     * 对两个位图进行 AND 操作，并将结果保存到目标位图
     *
     * @param destKey    目标位图的键
     * @param sourceKey1 源位图1的键
     * @param sourceKey2 源位图2的键
     * @return 操作是否成功
     */
    public Boolean bitAnd(String destKey, String sourceKey1, String sourceKey2) {
        return write(() -> {
            RBitSet destBitSet = redissonClient.getBitSet(destKey);
            destBitSet.and(sourceKey1, sourceKey2);
            return true;
        }, () -> {
            RBitSet destBitSet = redissonClient2.getBitSet(destKey);
            destBitSet.and(sourceKey1, sourceKey2);
        }, "bitAnd");
    }

    /**
     * 对两个位图进行 OR 操作，并将结果保存到目标位图
     *
     * @param destKey    目标位图的键
     * @param sourceKey1 源位图1的键
     * @param sourceKey2 源位图2的键
     * @return 操作是否成功
     */
    public Boolean bitOr(String destKey, String sourceKey1, String sourceKey2) {
        return write(() -> {
            RBitSet destBitSet = redissonClient.getBitSet(destKey);
            destBitSet.or(sourceKey1, sourceKey2);
            return true;
        }, () -> {
            RBitSet destBitSet = redissonClient2.getBitSet(destKey);
            destBitSet.or(sourceKey1, sourceKey2);
        }, "bitOr");
    }

    /**
     * 对两个位图进行 XOR 操作，并将结果保存到目标位图
     *
     * @param destKey    目标位图的键
     * @param sourceKey1 源位图1的键
     * @param sourceKey2 源位图2的键
     * @return 操作是否成功
     */
    public Boolean bitXor(String destKey, String sourceKey1, String sourceKey2) {
        return write(() -> {
            RBitSet destBitSet = redissonClient.getBitSet(destKey);
            destBitSet.xor(sourceKey1, sourceKey2);
            return true;
        }, () -> {
            RBitSet destBitSet = redissonClient2.getBitSet(destKey);
            destBitSet.xor(sourceKey1, sourceKey2);
        }, "bitXor");
    }

    /**
     * 清空位图
     *
     * @param key 键
     * @return 操作是否成功
     */
    public Boolean clearBitSet(String key) {
        return write(() -> {
            RBitSet bitSet = redissonClient.getBitSet(key);
            bitSet.clear();
            return true;
        }, () -> {
            RBitSet bitSet = redissonClient2.getBitSet(key);
            bitSet.clear();
        }, "clearBitSet");
    }

    // ====================== HyperLogLog 操作 ======================

    /**
     * 添加元素到 HyperLogLog
     *
     * @param key   键
     * @param value 要添加的元素
     * @return 添加后集合中的基数估计值
     */
    public <T> Long pfadd(String key, T value) {
        return write(() -> {
            RHyperLogLog<T> hyperLogLog = redissonClient.getHyperLogLog(key);
            hyperLogLog.add(value);
            return hyperLogLog.count();
        }, () -> {
            RHyperLogLog<T> hyperLogLog = redissonClient2.getHyperLogLog(key);
            hyperLogLog.add(value);
        }, "hyperLogLogAdd");
    }

    /**
     * 批量添加元素到 HyperLogLog
     *
     * @param key    键
     * @param values 要添加的元素集合
     * @return 添加后集合中的基数估计值
     */
    public <T> Long pfaddAll(String key, Collection<T> values) {
        return write(() -> {
            RHyperLogLog<T> hyperLogLog = redissonClient.getHyperLogLog(key);
            hyperLogLog.addAll(values);
            return hyperLogLog.count();
        }, () -> {
            RHyperLogLog<T> hyperLogLog = redissonClient2.getHyperLogLog(key);
            hyperLogLog.addAll(values);
        }, "hyperLogLogAddBatch");
    }

    /**
     * 获取 HyperLogLog 的基数估计值
     *
     * @param key 键
     * @return 基数估计值
     */
    public Long pfcount(String key) {
        RHyperLogLog<?> hyperLogLog = redissonClient.getHyperLogLog(key);
        return hyperLogLog.count();
    }

    /**
     * 获取多个 HyperLogLog 的并集的基数估计值
     *
     * @param keys 键的集合
     * @return 基数估计值
     */
    public Long pfcountUnion(Collection<String> keys) {
        List<RHyperLogLog<Object>> hyperLogLogs = keys.stream()
                .map(redissonClient::getHyperLogLog)
                .toList();
        return hyperLogLogs.getFirst().countWith(keys.toArray(new String[0]));
    }

    /**
     * 将多个 HyperLogLog 合并为一个
     *
     * @param destKey    目标键
     * @param sourceKeys 源键的集合
     * @return 合并后的基数估计值
     */
    public Long pfmerge(String destKey, Collection<String> sourceKeys) {
        return write(() -> {
            RHyperLogLog<?> destHLL = redissonClient.getHyperLogLog(destKey);
            destHLL.mergeWith(sourceKeys.toArray(new String[0]));
            return destHLL.count();
        }, () -> {
            RHyperLogLog<?> destHLL = redissonClient2.getHyperLogLog(destKey);
            destHLL.mergeWith(sourceKeys.toArray(new String[0]));
        }, "hyperLogLogMerge");
    }

    /**
     * 删除 HyperLogLog
     *
     * @param key 键
     * @return 是否删除成功
     */
    public Boolean pfdelete(String key) {
        return write(() -> {
            RHyperLogLog<?> hyperLogLog = redissonClient.getHyperLogLog(key);
            return hyperLogLog.delete();
        }, () -> {
            RHyperLogLog<?> hyperLogLog = redissonClient2.getHyperLogLog(key);
            hyperLogLog.delete();
        }, "hyperLogLogDelete");
    }

    // ====================== 原子操作 ======================
    /**
     * 递增
     */
    public long increment(String key, Duration duration) {
        return write(() -> {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            long added = atomicLong.addAndGet(1);
            atomicLong.expireIfNotSet(duration);
            return added;
        }, () -> {
            RAtomicLong atomicLong = redissonClient2.getAtomicLong(key);
            atomicLong.addAndGet(1);
            atomicLong.expireIfNotSet(duration);
        }, "increment");
    }

    /**
     * 递减
     */
    public long decrement(String key) {
        return write(() -> {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            return atomicLong.decrementAndGet();
        }, () -> {
            RAtomicLong atomicLong = redissonClient2.getAtomicLong(key);
            atomicLong.decrementAndGet();
        }, "decrement");
    }

    // ====================== 通用操作 ======================
    /**
     * 删除key
     */
    public boolean delete(String key) {
        return write(() -> {
            redissonClient.getBucket(key).delete();
            return true;
        }, () -> {
            redissonClient2.getBucket(key).delete();
        }, "delete");
    }

    /**
     * 批量删除key
     */
    public void delete(Collection<String> keys) {
        write(() -> {
            for (String key : keys) {
                redissonClient.getBucket(key).unlink();
            }
            return null;
        }, () -> {
            for (String key : keys) {
                redissonClient2.getBucket(key).unlink();
            }
        }, "batchDelete");
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, Duration duration) {
        return write(() -> {
            redissonClient.getBucket(key).expire(duration);
            return true;
        }, () -> {
            redissonClient2.getBucket(key).expire(duration);
        }, "expire");
    }

    /**
     * 判断key是否存在
     */
    public boolean exists(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    /**
     * 获取key的剩余过期时间
     */
    public long getExpire(String key) {
        return redissonClient.getBucket(key).remainTimeToLive();
    }

    // ====================== 私有方法 ======================
    /**
     * 写操作包装方法，支持双机房同步
     */
    private <T> T write(Supplier<T> action, Runnable backAction, String actionName) {
        T result = null;
        try {
            result = action.get();
            if (Boolean.TRUE.equals(redisProperties.getCluster2().getActive())) {
                try {
                    otherExecutor.execute(backAction::run);
                } catch (Exception e) {
                    LOGGER.error("{}, 异地redis异常:", actionName, e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("redisson {}, 操作异常: ", actionName, e);
        }
        return result;
    }

    /**
     * 执行写操作，并将action的返回值传递给backAction
     *
     * @param action 主操作
     * @param backAction 备份操作，接收主操作的返回值
     * @param actionName 操作名称，用于日志记录
     * @param <T> 返回值类型
     * @return 主操作的返回
     */
    private <T> T writeWithResult(Supplier<T> action, Consumer<T> backAction, String actionName) {
        T result = null;
        try {
            result = action.get();
            if (Boolean.TRUE.equals(redisProperties.getCluster2().getActive())) {
                try {
                    T finalResult = result;
                    otherExecutor.execute(() -> {
                        backAction.accept(finalResult);
                    });
                } catch (Exception e) {
                    LOGGER.error("{} - 异地redis异常: ", actionName, e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("redisson {} - 操作异常: ", actionName, e);
        }
        return result;
    }
}
