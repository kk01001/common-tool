package io.github.kk01001.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.*;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.ScoredEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author kk01001
 * date 2021/7/12 14:17
 * 多机房redisson操作 工具类
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
     * 初始化
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

    /**
     * 根据机房位置获取RedissonClient
     *
     * @param location 机房位置
     * @return RedissonClient
     */
    public RedissonClient getRedissonClient(String location) {
        return REDISSON_CLIENT_MAP.get(location);
    }

    /**
     * set
     */
    public <V> Boolean set(String key, V data) {
        try {
            redissonClient.getBucket(key).set(data);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getBucket(key).set(data);
                    } catch (Exception e) {
                        LOGGER.error("异地 redisson setString操作异常: ", e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson setString操作异常: ", e);
            return false;
        }
        return true;
    }

    /**
     * set 序列化对象为json
     */
    public Boolean setSerialize(String key, Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redissonClient.getBucket(key).set(json);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getBucket(key).set(json);
                    } catch (Exception e) {
                        LOGGER.error("异地 redisson setString操作异常: ", e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson setString操作异常: ", e);
            return false;
        }
        return true;
    }

    /**
     * string set duration
     *
     * @param key      键
     * @param data     值
     * @param duration 超时时间
     * @return true/false
     */
    public <V> Boolean set(String key, V data, Duration duration) {
        try {
            redissonClient.getBucket(key).set(data, duration);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getBucket(key).set(data, duration);
                    } catch (Exception e) {
                        LOGGER.error("redisson setString操作异常: ", e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson setString操作异常: ", e);
            return false;
        }
        return true;
    }

    /**
     * string set ttl
     *
     * @param key        键
     * @param data       值
     * @param timeToLive 超时时间
     * @param timeUnit   时间单位
     * @return true/false
     */
    public <V> Boolean set(String key, V data, long timeToLive, TimeUnit timeUnit) {
        try {
            redissonClient.getBucket(key).set(data, timeToLive, timeUnit);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getBucket(key).set(data, timeToLive, timeUnit);
                    } catch (Exception e) {
                        LOGGER.error("redisson setString操作异常: ", e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson setString操作异常: ", e);
            return false;
        }
        return true;
    }

    /**
     * string set
     *
     * @param key  键
     * @param data 值
     * @return true/false
     */
    public Boolean setString(String key, String data) {
        try {
            try {
                redissonClient.getBucket(key).set(data);
            } catch (Exception e) {
                LOGGER.error("redisson setString操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    redissonClient2.getBucket(key).set(data);
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson setString操作异常: ", e);
            return false;
        }
        return true;
    }

    /**
     * string set ttl
     *
     * @param key        键
     * @param data       值
     * @param timeToLive 超时时间
     * @param timeUnit   时间单位
     * @return true/false
     */
    public Boolean setString(String key, String data, long timeToLive, TimeUnit timeUnit) {
        try {
            try {
                redissonClient.getBucket(key).set(data, timeToLive, timeUnit);
            } catch (Exception e) {
                LOGGER.error("redisson setString操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    redissonClient2.getBucket(key).set(data, timeToLive, timeUnit);
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson setString操作异常: ", e);
            return false;
        }
        return true;
    }

    /**
     * 获取整个string并转对象
     *
     * @param key   键
     * @param clazz 对象class类型
     * @param <T>   对象泛型
     * @return 对象
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
     * get 命令
     *
     * @param key 键
     * @param <V> 泛型
     * @return V
     */
    public <V> V get(String key) {
        RBucket<V> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    public <V> V get(Codec codec, String key) {
        RBucket<V> bucket = redissonClient.getBucket(key, codec);
        return bucket.get();
    }

    public String getString(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    public String getStringOfBack(String key) {
        if (redissonClient2 == null) {
            return "";
        }
        RBucket<String> bucket = redissonClient2.getBucket(key);
        return bucket.get();
    }

    public String getString(String location, String key) {
        RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    public Boolean isExistString(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    public Boolean isExistString(String location, String key) {
        RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
        return redissonClient.getBucket(key).isExists();
    }

    /**
     * string 递增计数
     *
     * @param key 键
     */
    public void increment(String key, Duration duration) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            atomicLong.addAndGet(1);
            atomicLong.expire(duration);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    RAtomicLong rAtomicLong = redissonClient2.getAtomicLong(key);
                    rAtomicLong.addAndGet(1);
                    rAtomicLong.expire(duration);
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson LongAdder increment 操作异常: ", e);
        }
    }

    public void decrement(String key) {
        try {
            redissonClient.getLongAdder(key).decrement();
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getLongAdder(key).decrement();
            }
        } catch (Exception e) {
            LOGGER.error("redisson LongAdder decrement 操作异常: ", e);
        }
    }

    // list

    public List<Object> getList(String key) {
        RList<Object> list = redissonClient.getList(key);
        return list.range(0, -1);
    }

    public List<String> readAllList(String key) {
        RList<String> list = redissonClient.getList(key);
        return list.readAll();
    }

    public void setList(String key, List<Object> objectList) {
        try {
            redissonClient.getList(key).addAllAsync(objectList);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getList(key).addAllAsync(objectList);
            }
        } catch (Exception e) {
            LOGGER.error("redisson setList 操作异常: ", e);
        }
    }

    public boolean setList(String key, String item, Duration duration) {
        try {
            RList<Object> list = redissonClient.getList(key);
            list.add(item);
            list.expire(duration);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        RList<Object> rList = redissonClient2.getList(key);
                        rList.add(item);
                        rList.expire(duration);
                    } catch (Exception e) {
                        LOGGER.error("other redisson setList操作异常: ", e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson setList 操作异常: ", e);
            return false;
        }
        return true;
    }

    public void setListString(String key, List<String> stringList) {
        try {
            try {
                redissonClient.getList(key).addAll(stringList);
            } catch (Exception e) {
                LOGGER.error("redisson setList 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getList(key).addAllAsync(stringList);
            }
        } catch (Exception e) {
            LOGGER.error("redisson setList 操作异常: ", e);
        }
    }

    public Boolean containList(String key, Object object) {
        RList<Object> list = redissonClient.getList(key);
        return list.contains(object);
    }

    public void removeList(String key, String value) {
        redissonClient.getList(key).remove(value);
        if (redisProperties.getCluster2().getActive()) {
            redissonClient2.getList(key).remove(value);
        }
    }

    public Boolean delDeque(String key) {
        boolean del;
        try {
            del = redissonClient.getDeque(key).unlink();
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getDeque(key).unlink();
            }
        } catch (Exception e) {
            LOGGER.error("redisson deque del 操作异常: ", e);
            return false;
        }
        return del;
    }

    public boolean setDequeString(String key, List<String> stringList) {
        boolean success = false;
        try {
            try {
                success = redissonClient.getDeque(key).addAll(stringList);
            } catch (Exception e) {
                LOGGER.error("redisson setList 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getDeque(key).addAll(stringList);
            }
        } catch (Exception e) {
            LOGGER.error("redisson setList 操作异常: ", e);
        }
        return success;
    }

    /**
     * 取出双向list 第一个元素
     */
    public String getDequeFirst(String key) {

        RDeque<String> deque = redissonClient.getDeque(key);
        // 取第一个
        String first = deque.pollFirst();

        if (redisProperties.getCluster2().getActive()) {
            if (StringUtils.hasLength(first)) {
                otherExecutor.execute(() -> {
                    try {
                        RDeque<Object> rDeque = redissonClient2.getDeque(key);
                        rDeque.remove(first);
                    } catch (Exception e) {
                        LOGGER.error("other redisson getDequeFirst remove操作异常: ", e);
                    }
                });
            }
        }

        return first;
    }

    public Boolean isExistDeque(String key) {
        return redissonClient.getDeque(key).isExists();
    }

    /**
     * 双向list 添加元素到最后一个
     */
    public boolean offerLastDeque(String key, String value) {
        RDeque<Object> deque = redissonClient.getDeque(key);
        boolean offerLast = deque.offerLast(value);
        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RDeque<Object> rDeque = redissonClient2.getDeque(key);
                    rDeque.offerLast(value);
                } catch (Exception e) {
                    LOGGER.error("other redisson offerLastDeque offerLast 操作异常: ", e);
                }
            });
        }
        return offerLast;
    }

    public Boolean containDeque(String key, String value) {
        RDeque<String> deque = redissonClient.getDeque(key);
        return deque.contains(value);
    }

    /**
     * deque 设置值
     */
    public Boolean setDeque(String key, List<String> stringList) {
        boolean addAll;
        try {
            addAll = redissonClient.getDeque(key).addAll(stringList);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getDeque(key).addAll(stringList);
            }
        } catch (Exception e) {
            LOGGER.error("redisson deque addAll 操作异常: ", e);
            return false;
        }
        return addAll;
    }

    public Set<String> getSet(String key) {
        RSet<String> set = redissonClient.getSet(key);
        return set.readAll();
    }

    public Set<String> getSet(String location, String key) {
        RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
        RSet<String> set = redissonClient.getSet(key);
        return set.readAll();
    }

    public Set<String> getSetString(String key) {
        RSet<String> set = redissonClient.getSet(key);
        return set.readAll();
    }

    public Set<String> getSetStringBatch(String... key) {

        RBatch batch = redissonClient.createBatch();
        for (String s : key) {
            batch.getSet(s).readAllAsync();
        }
        BatchResult<?> execute = batch.execute();
        List<?> responses = execute.getResponses();
        Set<String> result = new HashSet<>();
        for (Object res : responses) {
            Set<String> set = (Set<String>) res;
            result.addAll(set);
        }
        return result;
    }

    /**
     * 从set随机取出count个元素
     */
    public Set<Object> getRandomSetByCount(String key, int count) {
        return redissonClient.getSet(key).random(count);
    }

    public Integer getSetCount(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.size();
    }

    public Object getSetRandom(String key) {
        return redissonClient.getSet(key).random();
    }

    /**
     * set 随机移出一个元素
     */
    public String removeSetRandom(String key) {
        String random = "";
        try {
            try {
                random = String.valueOf(redissonClient.getSet(key).removeRandom());
            } catch (Exception e) {
                LOGGER.error("redisson removeSetRandom 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                if (StringUtils.hasLength(random)) {
                    String finalRandom = random;
                    otherExecutor.execute(() -> {
                        try {
                            redissonClient2.getSet(key).remove(finalRandom);
                        } catch (Exception e) {
                            LOGGER.error("异地redis异常: {}", e.getMessage());
                        }
                    });
                }
            }
        } catch (Exception e) {
            LOGGER.error("redisson removeSetRandom 操作异常: ", e);
        }
        return random;
    }

    public Set<Object> removeSetRandomByCount(String key, Integer count) {
        Set<Object> objects = null;
        try {
            try {
                objects = redissonClient.getSet(key).removeRandom(count);
            } catch (Exception e) {
                LOGGER.error("redisson removeSetRandom 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                if (!CollectionUtils.isEmpty(objects)) {
                    Set<Object> finalObjects = objects;
                    otherExecutor.execute(() -> {
                        try {
                            redissonClient2.getSet(key).removeAll(finalObjects);
                        } catch (Exception e) {
                            LOGGER.error("异地redis异常: {}", e.getMessage());
                        }
                    });
                }
            }
        } catch (Exception e) {
            LOGGER.error("redisson removeSetRandom 操作异常: ", e);
        }
        return objects;
    }

    /**
     * set 添加元素
     */
    public <V> Boolean addAllSet(String key, Duration expire, V... value) {
        if (Objects.isNull(value)) {
            return false;
        }
        boolean add = false;
        try {
            try {
                RSet<V> set = redissonClient.getSet(key);
                add = set.addAll(Arrays.asList(value));
                set.expire(expire);
            } catch (Exception e) {
                LOGGER.error("redisson addSet 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        RSet<V> rSet = redissonClient2.getSet(key);
                        rSet.addAll(Arrays.asList(value));
                        rSet.expire(expire);
                    } catch (Exception e) {
                        LOGGER.error("异地redis异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson addSet 操作异常: ", e);
        }
        return add;
    }

    /**
     * set 添加元素
     */
    public <V> Boolean addSet(String key, V value, Duration expire) {
        boolean add = false;
        try {
            try {
                RSet<V> set = redissonClient.getSet(key);
                add = set.add(value);
                set.expire(expire);
            } catch (Exception e) {
                LOGGER.error("redisson addSet 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        RSet<V> rSet = redissonClient2.getSet(key);
                        rSet.add(value);
                        rSet.expire(expire);
                    } catch (Exception e) {
                        LOGGER.error("异地redis异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson addSet 操作异常: ", e);
        }
        return add;
    }

    /**
     * set 添加元素
     */
    public Boolean addSet(String key, Object object) {
        boolean add = true;
        try {
            try {
                add = redissonClient.getSet(key).add(object);
            } catch (Exception e) {
                LOGGER.error("redisson addSet 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getSet(key).add(object);
                    } catch (Exception e) {
                        LOGGER.error("异地redis异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson addSet 操作异常: ", e);
        }
        return add;
    }

    public Boolean removeSet(String key, String object) {
        boolean success = false;
        try {
            if (!StringUtils.hasLength(object)) {
                return false;
            }
            try {
                success = redissonClient.getSet(key).remove(object);
            } catch (Exception e) {
                LOGGER.error("redisson removeSet 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getSet(key).remove(object);
                    } catch (Exception e) {
                        LOGGER.error("异地redis异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson removeSet 操作异常: ", e);
        }
        return success;
    }

    public Boolean containsSet(String key, Object object) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.contains(object);
    }

    public void addAllSet(String key, Set<String> objectSet) {
        try {
            redissonClient.getSet(key).addAll(objectSet);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getSet(key).addAll(objectSet);
            }
        } catch (Exception e) {
            LOGGER.error("redisson addAllSet 操作异常: ", e);
        }
    }

    public Boolean addAllSet(String key, List<String> valueList) {
        boolean success = false;
        try {
            success = redissonClient.getSet(key).addAll(valueList);
        } catch (Exception e) {
            LOGGER.error("redisson addAllSet 操作异常: {}", e.getMessage());
        }

        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    redissonClient2.getSet(key).addAll(valueList);
                } catch (Exception e) {
                    LOGGER.error("异地redis addAllSet异常: {}", e.getMessage());
                }
            });
        }
        return success;
    }

    public Boolean delSet(String key) {
        boolean success = false;
        try {
            try {
                success = redissonClient.getSet(key).unlink();
            } catch (Exception e) {
                LOGGER.error("redisson delSet 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getSet(key).unlink();
                    } catch (Exception e) {
                        LOGGER.error("异地redis异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson delSet 操作异常: ", e);
        }
        return success;
    }

    public void setSetString(String key, List<String> stringList) {
        try {
            try {
                redissonClient.getSet(key).addAll(stringList);
            } catch (Exception e) {
                LOGGER.error("redisson setSetString List<String> 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getSet(key).addAll(stringList);
                    } catch (Exception e) {
                        LOGGER.error("异地redis异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson setSetString List<String> 操作异常: ", e);
        }
    }

    public Boolean isExistSet(String key) {
        return redissonClient.getSet(key).isExists();
    }

    public void setSet(String key, Set<Object> objectList) {
        try {
            redissonClient.getSet(key).addAll(objectList);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getSet(key).addAll(objectList);
            }
        } catch (Exception e) {
            LOGGER.error("redisson setSet Set<Object> 操作异常: ", e);
        }
    }

    public void setSetString(String key, Set<String> objectList) {
        try {
            redissonClient.getSet(key).addAll(objectList);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getSet(key).addAll(objectList);
            }
        } catch (Exception e) {
            LOGGER.error("redisson setSetString Set<String> 操作异常: ", e);
        }
    }

    public void setSet(String key, Object object) {
        try {
            redissonClient.getSet(key).add(object);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getSet(key).add(object);
            }
        } catch (Exception e) {
            LOGGER.error("redisson setSet Object 操作异常: ", e);
        }
    }

    // hash

    /**
     * 查询hash 某一项
     *
     * @param key  键
     * @param item 项
     * @param <K>  hash key
     * @param <V>  hash value
     * @return value
     */
    public <K, V> V getHashByItem(String key, K item) {
        RMap<K, V> map = redissonClient.getMap(key);
        return map.get(item);
    }

    /**
     * 指定机房 查询hash 某一项
     *
     * @param key      键
     * @param item     项
     * @param location 机房
     * @param <K>      hash key
     * @param <V>      hash value
     * @return value
     */
    public <K, V> V getHashByItem(String location, String key, K item) {
        RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
        RMap<K, V> map = redissonClient.getMap(key);
        return map.get(item);
    }

    /**
     * 移除hash某一项
     *
     * @param key  键
     * @param item 项
     * @return bool
     */
    public boolean removeHashItem(String key, String item) {
        try {
            redissonClient.getMap(key).remove(item);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getMap(key).remove(item);
                    } catch (Exception e) {
                        LOGGER.error("removeHashItem, key: {}, item: {}, 异地redis操作异常: ", key, item, e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("removeHashItem, key: {}, item: {}, redis操作异常: ", key, item, e);
            return false;
        }
        return true;
    }

    /**
     * 批量移除hash
     *
     * @param key   键
     * @param items 项
     * @return bool
     */
    public boolean fastRemoveHashItem(String key, List<String> items) {
        try {
            redissonClient.getMap(key).fastRemove(items);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getMap(key).fastRemove(items);
                    } catch (Exception e) {
                        LOGGER.error("fastRemoveHashItem, key: {}, item: {}, 异地redis操作异常: ", key, items, e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("fastRemoveHashItem, key: {}, item: {}, redis操作异常: ", key, items, e);
            return false;
        }
        return true;
    }

    public void delHash(String key) {
        try {
            redissonClient.getMap(key).unlink();
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getMap(key).unlink();
            }
        } catch (Exception e) {
            LOGGER.error("redisson delHash 操作异常: ", e);
        }
    }

    public void setHash(String key, Map<String, Object> dataMap) {
        try {
            redissonClient.getMap(key).putAll(dataMap, 100);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getMap(key).putAll(dataMap, 100);
            }
        } catch (Exception e) {
            LOGGER.error("redisson setHash Map 操作异常: ", e);
        }
    }

    /**
     * 获取整个hash
     *
     * @param key   键
     * @param clazz 对象class类型
     * @param <T>   对象泛型
     * @return 对象
     */
    public <T> T getHash(String key, Class<T> clazz) {
        Map<Object, Object> allMap = redissonClient.getMap(key).readAllMap();
        return objectMapper.convertValue(allMap, clazz);
    }

    public Integer getHashCount(String key) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return map.size();
    }

    public Map<String, String> getStringMap(String key) {
        Map<Object, Object> objectObjectMap = redissonClient.getMap(key).readAllMap();
        return objectObjectMap.entrySet().stream().collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
    }

    /**
     * 读取hash kv
     *
     * @param key 键
     * @param <K> k
     * @param <V> v
     * @return map
     */
    public <K, V> Map<K, V> readAllHash(String key) {
        RMap<K, V> map = redissonClient.getMap(key);
        return map.readAllMap();
    }

    /**
     * 读取hash item
     *
     * @param key 键
     * @param <K> k
     * @return map
     */
    public <K, V> Set<K> readAllHashKey(String key) {
        RMap<K, V> map = redissonClient.getMap(key);
        return map.keySet();
    }

    /**
     * 设置hash
     *
     * @param key  键
     * @param <T>  class类型
     * @param data 对象-转map
     * @return success
     */
    public <T> Boolean setHash(String key, T data) {
        boolean success = false;
        try {
            Map<String, Object> objectMap = objectMapper.convertValue(data, new TypeReference<Map<String, Object>>() {
            });
            redissonClient.getMap(key).putAll(objectMap, 100);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getMap(key).putAll(objectMap, 100);
                    } catch (Exception e) {
                        LOGGER.error("异地redis setHash异常: {}", e.getMessage());
                    }
                });
            }
            success = true;
        } catch (Exception e) {
            LOGGER.error("redisson setHash异常: ", e);
        }
        return success;
    }

    public <T> Boolean setHash(String key, T data, Duration duration) {
        boolean success = false;
        try {
            Map<String, Object> objectMap = objectMapper.convertValue(data, new TypeReference<>() {
            });
            RMap<Object, Object> map = redissonClient.getMap(key);
            map.putAll(objectMap, 100);
            map.expire(duration);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        RMap<Object, Object> rMap = redissonClient2.getMap(key);
                        rMap.putAll(objectMap, 100);
                        rMap.expire(duration);
                    } catch (Exception e) {
                        LOGGER.error("异地redis setHash data异常: {}", e.getMessage());
                    }
                });
            }
            success = true;
        } catch (Exception e) {
            LOGGER.error("redisson setHash异常: ", e);
        }
        return success;
    }

    public <K, V> Boolean setHash(String key, K item, V data, Duration expire) {
        try {
            RMap<K, V> map = redissonClient.getMap(key);
            map.put(item, data);
            map.expire(expire);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        RMap<Object, Object> rMap = redissonClient2.getMap(key);
                        rMap.put(item, data);
                        rMap.expire(expire);
                    } catch (Exception e) {
                        LOGGER.error("redisson setHash item Object 操作异常: ", e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson setHash item Object 操作异常: ", e);
            return false;
        }
        return true;
    }

    public <V> Boolean setHash(String key, String item, V data) {
        try {
            redissonClient.getMap(key).put(item, data);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    redissonClient2.getMap(key).put(item, data);
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson setHash item Object 操作异常: ", e);
            return false;
        }
        return true;
    }

    /**
     * redisson lock
     *
     * @param key 锁key
     */
    public RLock lock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.lock(10, TimeUnit.SECONDS);
        return lock;
    }

    public void lock(String key, long leaseTime) {
        RLock lock = redissonClient.getLock(key);
        lock.lock(leaseTime, TimeUnit.SECONDS);
    }

    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.tryLock(waitTime, leaseTime, unit);
    }

    // zset

    /**
     * 批量添加zset元素 分数
     */
    public Integer addAllZset(String key, Map<Object, Double> objects) {
        int i = 0;
        try {
            i = redissonClient.getScoredSortedSet(key).addAll(objects);
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getScoredSortedSet(key).addAll(objects);
            }
        } catch (Exception e) {
            LOGGER.error("redisson addAllZset 操作异常: ", e);
        }
        return i;
    }

    /**
     * zset key 是否存在
     */
    public Boolean isExistZsetKey(String key) {
        return redissonClient.getScoredSortedSet(key).isExists();
    }

    /**
     * zset 添加一个元素
     */
    public Boolean addZset(String key, String value, double score) {
        boolean add = false;
        try {
            add = redissonClient.getScoredSortedSet(key).add(score, value);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getScoredSortedSet(key).add(score, value);
                    } catch (Exception e) {
                        LOGGER.error("异地redis异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson addZset 操作异常: ", e);
        }
        return add;
    }

    /**
     * zset 添加一个元素
     */
    public void addScoreZsetIfAbsent(String key, String value, double score) {
        try {
            redissonClient.getScoredSortedSet(key).addIfAbsent(score, value);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getScoredSortedSet(key).addIfAbsent(score, value);
                    } catch (Exception e) {
                        LOGGER.error("addScoreZsetIfAbsent, 异地redis异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("addScoreZsetIfAbsent, redisson操作异常: ", e);
        }
    }

    /**
     * zset 添加一个元素
     */
    public Double addScoreZset(String key, String value, double score) {
        Double addScore = 0D;
        try {
            addScore = redissonClient.getScoredSortedSet(key).addScore(value, score);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getScoredSortedSet(key).addScore(value, score);
                    } catch (Exception e) {
                        LOGGER.error("addScoreZset, 异地redis异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("addScoreZset, redisson操作异常: ", e);
        }
        return addScore;
    }

    public ScoredEntry<String> popZsetMinScore(String key) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(key);
        return sortedSet.pollFirstEntry();
    }

    /**
     * 获取zset 全部member和score
     *
     * @param key 键
     * @return list
     */
    public List<ScoredEntry<String>> getZset(String key) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(key);
        Iterator<ScoredEntry<String>> iterator = sortedSet.entryIterator();
        List<ScoredEntry<String>> list = new ArrayList<>();
        while (iterator.hasNext()) {
            ScoredEntry<String> entry = iterator.next();
            list.add(entry);
        }
        return list;
    }

    /**
     * 获取zset 全部member和score
     *
     * @param key 键
     * @return list
     */
    public List<ScoredEntry<String>> getZsetAsc(String key) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(key);
        Collection<ScoredEntry<String>> scoredEntries = sortedSet.entryRange(0, -1);
        return new ArrayList<>(scoredEntries);
    }

    /**
     * 获取zset 从小到大
     *
     * @param key 键
     * @return list
     */
    public List<String> readAllAscZset(String key) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(key);
        Collection<String> all = sortedSet.readAll();
        if (CollectionUtils.isEmpty(all)) {
            return null;
        }
        return new ArrayList<>(all);
    }

    /**
     * 获取zset 从小到大  读取一段范围
     *
     * @param key 键
     * @return list
     */
    public List<String> readRangeAscZset(String key, int startIndex, int endIndex) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(key);
        Collection<String> all = sortedSet.valueRange(startIndex, endIndex);
        if (CollectionUtils.isEmpty(all)) {
            return null;
        }
        return new ArrayList<>(all);
    }

    /**
     * 获取zset 从大到小
     *
     * @param key 键
     * @return list
     */
    public List<String> readAllDescZset(String key) {
        RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(key);
        Collection<String> all = sortedSet.valueRangeReversed(0, -1);
        if (CollectionUtils.isEmpty(all)) {
            return null;
        }
        return new ArrayList<>(all);
    }

    /**
     * 移除整个zset
     */
    public Boolean deleteZSet(String key) {
        boolean bool = false;
        try {
            bool = redissonClient.getScoredSortedSet(key).unlink();
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getScoredSortedSet(key).unlink();
            }
        } catch (Exception e) {
            LOGGER.error("redisson deleteZSet 操作异常: ", e);
        }
        return bool;
    }

    /**
     * 获取zset score和value
     *
     * @param key        键
     * @param startIndex 开始索引 0 开始
     * @param endIndex   结束索引
     * @return ScoredEntry
     */
    public <T> ScoredEntry<T> getZsetScoredEntry(String key, int startIndex, int endIndex) {
        try {
            RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
            Collection<ScoredEntry<T>> scoredEntries = scoredSortedSet.entryRange(startIndex, endIndex);
            Optional<ScoredEntry<T>> first = scoredEntries.stream().findFirst();
            if (first.isPresent()) {
                return scoredEntries.stream().findFirst().get();
            }
        } catch (Exception e) {
            LOGGER.error("redisson getZsetScoredEntry 操作异常: ", e);
        }
        return null;
    }

    /**
     * 获取zset score和value
     *
     * @param key        键
     * @param startIndex 开始索引 0 开始
     * @param endIndex   结束索引
     * @return ScoredEntry
     */
    public <T> List<ScoredEntry<T>> getZsetScoredEntryList(String key, int startIndex, int endIndex) {
        try {
            RScoredSortedSet<T> scoredSortedSet = redissonClient.getScoredSortedSet(key);
            return new ArrayList<>(scoredSortedSet.entryRange(startIndex, endIndex));
        } catch (Exception e) {
            LOGGER.error("redisson getZsetScoredEntry 操作异常: ", e);
        }
        return null;
    }

    /**
     * zset 移除头一个元素
     */
    public void removeZsetFirst(String key) {
        try {
            redissonClient.getScoredSortedSet(key).pollFirst();
            if (redisProperties.getCluster2().getActive()) {
                redissonClient2.getScoredSortedSet(key).pollFirst();
            }
        } catch (Exception e) {
            LOGGER.error("redisson getZsetScoredEntry 操作异常: ", e);
        }
    }

    /**
     * 移除一个元素
     *
     * @param key    键
     * @param object 元素
     */
    public void removeZsetObject(String key, Object object) {
        redissonClient.getScoredSortedSet(key).remove(object);
        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    redissonClient2.getScoredSortedSet(key).remove(object);
                } catch (Exception e) {
                    LOGGER.error("异地redis removeZsetObject 异常: {}", e.getMessage());
                }
            });
        }
    }

    /**
     * set  批量移除元素
     *
     * @param key        键
     * @param collection 集合
     * @return 是否成功
     */
    public Boolean removeSetBatch(String key, Collection<String> collection) {
        boolean success = false;
        try {
            success = redissonClient.getSet(key).removeAll(collection);
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getSet(key).removeAll(collection);
                    } catch (Exception e) {
                        LOGGER.error("异地redis removeSetBatch异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson removeSetBatch 操作异常: ", e);
        }
        return success;
    }

    // ttl

    public Boolean isNotExistString(String key) {
        long l = redissonClient.getBucket(key).remainTimeToLive();

        return -2 == l;
    }

    public Boolean isNotExistString2(String key) {
        long l = redissonClient2.getBucket(key).remainTimeToLive();

        return -2 == l;
    }

    public Boolean existSet(String key) {
        long l = redissonClient.getSet(key).remainTimeToLive();
        return -2 == l;
    }

    /**
     * 删除key
     */
    public Boolean delKey(String key) {
        boolean success = false;
        try {
            try {
                success = redissonClient.getBucket(key).unlink();
            } catch (Exception e) {
                LOGGER.error("redisson delKey 操作异常: ", e);
            }
            if (redisProperties.getCluster2().getActive()) {
                otherExecutor.execute(() -> {
                    try {
                        redissonClient2.getBucket(key).unlink();
                    } catch (Exception e) {
                        LOGGER.error("异地redis delKey异常: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("redisson delKey 操作异常: ", e);
        }
        return success;
    }

    /**
     * 设置锁
     * 过期时间单位 分钟
     */
    public <V> boolean setNx(String key, V value, Duration duration) {
        RBucket<V> bucket = redissonClient.getBucket(key);
        return bucket.setIfAbsent(value, duration);
    }

    /**
     * 设置key 过期时间
     * 过期时间单位 s
     */
    public boolean setTTL(String key, long expire) {
        boolean bool = redissonClient.getBucket(key).expire(expire, TimeUnit.SECONDS);
        if (redisProperties.getCluster2().getActive()) {
            redissonClient2.getBucket(key).expire(expire, TimeUnit.SECONDS);
        }
        return bool;
    }

    /**
     * 设置key 过期时间
     * 过期时间单位 s
     */
    public boolean setTTL(String key, Duration duration) {
        boolean bool = redissonClient.getBucket(key).expire(duration);
        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    redissonClient2.getBucket(key).expire(duration);
                } catch (Exception e) {
                    LOGGER.error("异地redis异常: {}", e.getMessage());
                }
            });
        }
        return bool;
    }

    public Long getTtl(String key) {
        return redissonClient.getBucket(key).remainTimeToLive();
    }

    public void setSetsBatch(List<String> keys, List<String> list) {
        RBatch batch = redissonClient.createBatch();
        for (String key : keys) {
            batch.getSet(key).addAllAsync(list);
        }
        batch.execute();
        if (redisProperties.getCluster2().getActive()) {
            RBatch batch2 = redissonClient2.createBatch();
            for (String key : keys) {
                batch2.getSet(key).addAllAsync(list);
            }
            batch2.execute();
        }
    }

    public void removeSetsBatch(List<String> keys, List<String> list) {
        RBatch batch = redissonClient.createBatch();
        for (String key : keys) {
            batch.getSet(key).removeAllAsync(list);
        }
        batch.execute();
        if (redisProperties.getCluster2().getActive()) {
            RBatch batch2 = redissonClient2.createBatch();
            for (String key : keys) {
                batch2.getSet(key).removeAllAsync(list);
            }
            batch2.execute();
        }
    }

    /**
     * hash 元素递增
     *
     * @param key        hash 键
     * @param item       项
     * @param delta      递增量
     * @param timeToLive 过期时间
     * @param timeUnit   单位
     * @return 结果
     */
    public Long addHashItem(String key, String item, Integer delta, long timeToLive, TimeUnit timeUnit) {
        RMap<String, Long> map = redissonClient.getMap(key);
        map.expire(timeToLive, timeUnit);
        return map.addAndGet(item, delta);
    }

    /**
     * hash 元素递增
     *
     * @param location   所属机房编码
     * @param key        hash 键
     * @param item       项
     * @param delta      递增量
     * @param timeToLive 过期时间
     * @param timeUnit   单位
     * @return 结果
     */
    public Long addHashItem(String location, String key, String item, long delta, long timeToLive, TimeUnit timeUnit) {
        RedissonClient redissonClient = REDISSON_CLIENT_MAP.get(location);
        RMap<String, Long> map = redissonClient.getMap(key);
        Long aLong = map.addAndGet(item, delta);
        map.expire(timeToLive, timeUnit);
        return aLong;
    }

    /**
     * 获取 redisson公平锁
     *
     * @param lockKey 锁键
     * @return redisson公平锁
     */
    public RLock getFairLock(String lockKey) {
        return redissonClient.getFairLock(lockKey);
    }

    /**
     * 获取 redisson锁
     *
     * @param lockKey 锁键
     * @return redisson锁
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    public <T> void offerBlockList(String key, T data) {
        RBlockingQueue<T> blockingQueue = redissonClient.getBlockingQueue(key);
        blockingQueue.offer(data);
        if (redisProperties.getCluster2().getActive()) {
            otherExecutor.execute(() -> {
                try {
                    RBlockingQueue<T> blockingQueue2 = redissonClient2.getBlockingQueue(key);
                    blockingQueue2.offer(data);
                } catch (Exception e) {
                    LOGGER.error("异地 offerBlockList异常: {}", e.getMessage());
                }
            });
        }
    }

    public <T> T pollBlockList(String key) {
        RBlockingQueue<T> blockingQueue = redissonClient.getBlockingQueue(key);
        blockingQueue.poll();
        return blockingQueue.poll();
    }

    public <T> List<T> pollBlockList(String key, int limit) {
        RBlockingQueue<T> blockingQueue = redissonClient.getBlockingQueue(key);
        return blockingQueue.poll(limit);
    }

    public <T> T pollBlockList(String key, long timeout, TimeUnit unit) throws InterruptedException {
        RBlockingQueue<T> blockingQueue = redissonClient.getBlockingQueue(key);
        return blockingQueue.poll(timeout, unit);
    }
}
