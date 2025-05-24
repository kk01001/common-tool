package io.github.kk01001.examples.redis;

import io.github.kk01001.redis.core.MultiRedisClientManager;
import io.github.kk01001.redis.util.RedissonUtil;
import io.github.kk01001.util.JacksonUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author kk01001
 * @date 2023-12-15 10:31:00
 * @description 多Redis实例使用示例
 */
@Component
public class MultiRedisExample implements CommandLineRunner {

    /**
     * Redis客户端管理器
     */
    @Autowired
    private MultiRedisClientManager redisClientManager;

    /**
     * Redis工具类
     */
    @Autowired
    private RedissonUtil redissonUtil;

    /**
     * 主Redis客户端
     */
    @Autowired
    @Qualifier("redissonClient")
    private RedissonClient redissonClient;

    /**
     * 备用Redis客户端（可能为null）
     */
    @Autowired(required = false)
    @Qualifier("redissonClient2")
    private RedissonClient redissonClient2;

    /**
     * RedisTemplate
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 默认实例名称
     */
    @Value("${multi.redis.default-instance:master}")
    private String defaultInstanceName;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("======= 多Redis实例使用示例 =======");

        // 1. 获取所有实例名称
        System.out.println("\n1. 已配置的Redis实例:");
        redisClientManager.getInstanceNames().forEach(name ->
                System.out.println("  - " + name + (name.equals(defaultInstanceName) ? " (默认)" : "")));

        // 2. 使用RedissonUtil进行基本操作
        System.out.println("\n2. RedissonUtil基本操作:");

        // 字符串操作
        redissonUtil.set("test:string", "Hello Multi-Redis");
        System.out.println("  读取字符串: " + redissonUtil.get("test:string"));

        // 设置带过期时间的值
        redissonUtil.set("test:expire", "将在10秒后过期", Duration.ofSeconds(10));
        System.out.println("  带过期时间的值: " + redissonUtil.get("test:expire"));

        // 哈希表操作
        redissonUtil.hset("test:hash", "field1", "value1");
        redissonUtil.hset("test:hash", "field2", "value2");
        System.out.println("  哈希表field1: " + redissonUtil.hget("test:hash", "field1"));
        System.out.println("  哈希表所有字段: " + redissonUtil.hgetAll("test:hash"));

        // 3. 使用指定实例
        System.out.println("\n3. 使用指定实例:");

        // 获取默认实例名称
        System.out.println("  默认实例: " + defaultInstanceName);

        // 直接使用RedissonClient操作指定实例
        if (redisClientManager.getInstanceNames().size() > 0) {
            String instanceName = redisClientManager.getInstanceNames().iterator().next();
            RedissonClient client = redisClientManager.getClient(instanceName);
            client.getBucket("instance:specific").set("使用指定实例写入");
            System.out.println("  从指定实例读取: " + client.getBucket("instance:specific").get());
        }

        // 4. 使用RedisTemplate
        System.out.println("\n4. 使用RedisTemplate:");

        // 字符串操作
        redisTemplate.opsForValue().set("template:string", "RedisTemplate测试");
        System.out.println("  RedisTemplate读取: " + redisTemplate.opsForValue().get("template:string"));

        // 哈希操作
        redisTemplate.opsForHash().put("template:hash", "name", "张三");
        redisTemplate.opsForHash().put("template:hash", "age", "30");
        System.out.println("  RedisTemplate哈希值: " + redisTemplate.opsForHash().entries("template:hash"));

        // 5. 分布式锁示例
        System.out.println("\n5. 分布式锁示例:");
        distributedLockExample();

        // 6. 多实例写入示例
        System.out.println("\n6. 多实例写入示例:");
        multiInstanceWriteExample();

        // 7. 对象存储示例
        System.out.println("\n7. 对象存储示例:");
        objectStorageExample();

        System.out.println("\n======= 示例结束 =======");
    }

    /**
     * 分布式锁示例
     */
    private void distributedLockExample() {
        String lockKey = "test:lock:order:12345";
        RLock lock = redissonUtil.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待5秒，锁过期时间为10秒
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);

            if (acquired) {
                System.out.println("  获取到分布式锁: " + lockKey);

                // 模拟业务处理
                Thread.sleep(1000);
                System.out.println("  完成锁内业务处理");
            } else {
                System.out.println("  未能获取到锁: " + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("  锁获取被中断: " + e.getMessage());
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println("  释放分布式锁: " + lockKey);
            }
        }

        // 使用指定实例的锁
        if (redisClientManager.getInstanceNames().size() > 1) {
            String instanceName = redisClientManager.getInstanceNames().stream()
                    .filter(name -> !name.equals(defaultInstanceName))
                    .findFirst().orElse(null);

            if (instanceName != null) {
                System.out.println("  使用实例 " + instanceName + " 的锁");
                // 直接使用实例获取锁
                RedissonClient client = redisClientManager.getClient(instanceName);
                RLock specificLock = client.getLock("specific:lock");

                try {
                    specificLock.lock(5, TimeUnit.SECONDS);
                    System.out.println("  获取到指定实例的锁");
                } finally {
                    if (specificLock.isHeldByCurrentThread()) {
                        specificLock.unlock();
                        System.out.println("  释放指定实例的锁");
                    }
                }
            }
        }
    }

    /**
     * 多实例写入示例
     */
    private void multiInstanceWriteExample() {
        // 获取所有可用实例
        for (String instanceName : redisClientManager.getInstanceNames()) {
            RedissonClient client = redisClientManager.getClient(instanceName);

            // 写入实例特定的数据
            RBucket<String> bucket = client.getBucket("multi:instance:test:" + instanceName);
            bucket.set("这是写入到 " + instanceName + " 实例的数据");

            System.out.println("  写入实例 " + instanceName + " 的数据: " + bucket.get());
        }

        // 使用多个实例
        for (String instanceName : redisClientManager.getInstanceNames()) {
            RedissonClient client = redisClientManager.getClient(instanceName);
            client.getBucket("util:multi:test").set("写入 " + instanceName);
            System.out.println("  读取 " + instanceName + ": " +
                    client.getBucket("util:multi:test").get());
        }
    }

    /**
     * 对象存储示例
     */
    private void objectStorageExample() {
        // 创建用户对象
        User user = new User();
        user.setId(1001L);
        user.setUsername("zhangsan");
        user.setAge(30);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("email", "zhangsan@example.com");
        attributes.put("phone", "13800138000");
        user.setAttributes(attributes);

        // 使用RedisTemplate存储对象
        redisTemplate.opsForValue().set("user:" + user.getId(), JacksonUtil.toJson(user));
        Object retrievedUser = redisTemplate.opsForValue().get("user:" + user.getId());
        System.out.println("  通过RedisTemplate存储和检索对象: " + retrievedUser);

        // 使用Redisson存储对象
        RMap<String, String> userMap = redissonClient.getMap("users");
        userMap.put(user.getUsername(), JacksonUtil.toJson(user));
        String redissonUser = userMap.get(user.getUsername());
        System.out.println("  通过Redisson存储和检索对象: " + redissonUser);

        // 使用RedissonUtil存储对象
        redissonUtil.set("util:user:" + user.getId(), JacksonUtil.toJson(user));
        try {
            User utilUser = redissonUtil.get("util:user:" + user.getId(), User.class);
            System.out.println("  通过RedissonUtil存储和检索对象: " + utilUser);
        } catch (Exception e) {
            System.out.println("  通过RedissonUtil检索对象失败: " + e.getMessage());
        }
    }

    /**
     * 用户类，用于对象存储示例
     */
    public static class User {
        private Long id;
        private String username;
        private int age;
        private Map<String, String> attributes;

        // Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", age=" + age +
                    ", attributes=" + attributes +
                    '}';
        }
    }
} 