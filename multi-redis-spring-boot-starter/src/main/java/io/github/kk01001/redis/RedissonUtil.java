package io.github.kk01001.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RDeque;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
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
    private DoubleRedisProperties redisProperties;

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
    public <V> V hget(String key, String field) {
        RMap<String, V> map = redissonClient.getMap(key);
        return map.get(field);
    }

    /**
     * 获取所有hash字段和值
     */
    public <V> Map<String, V> hgetAll(String key) {
        RMap<String, V> map = redissonClient.getMap(key);
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
     * @param key   键
     * @param value 要检查的元素
     * @return 如果列表包含指定元素，则返回true
     */
    public <V> Boolean lcontains(String key, V value) {
        RList<V> list = redissonClient.getList(key);
        return list.contains(value);
    }

    /**
     * 检查列表中是否包含指定的所有元素
     *
     * @param key    键
     * @param values 要检查的元素集合
     * @return 如果列表包含指定的所有元素，则返回true
     */
    public <V> Boolean lcontainsAll(String key, Collection<V> values) {
        RList<V> list = redissonClient.getList(key);
        return list.containsAll(values);
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
     * @param key    键
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
     * @param key   键
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
     * @param key   键
     * @param start 开始位置（包含）
     * @param end   结束位置（包含）
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
     * @param key    键
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
     * @param key   键
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
        RDeque<V> deque = redissonClient.getDeque(key);
        return deque.pollFirst();
    }

    /**
     * 获取并移除队尾元素
     */
    public <V> V pollLast(String key) {
        RDeque<V> deque = redissonClient.getDeque(key);
        return deque.pollLast();
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
                    backAction.run();
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
                    backAction.accept(result);
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
