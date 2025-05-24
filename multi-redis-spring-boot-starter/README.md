# Multi Redis Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/multi-redis-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.kk01001/multi-redis-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 基于 [Redisson](https://github.com/redisson/redisson) 的多Redis实例配置组件，支持同时连接多个Redis集群，实现数据多机房同步和读写分离。

## 背景

在大型分布式系统中，特别是跨区域部署的应用场景下，通常需要将数据同时写入多个Redis集群以实现数据容灾备份和就近访问。传统的单Redis实例配置无法满足这一需求，需要开发者手动管理多个Redis连接和数据同步逻辑，增加了开发复杂度和出错风险。

`multi-redis-spring-boot-starter` 提供了一种简单的方式来配置和管理多个Redis实例，实现数据的多集群同步写入和灵活读取，特别适合跨机房、多区域部署的应用场景。

## 项目目标

- **简单配置**：通过简单的YAML配置即可连接多个Redis集群
- **异步同步**：支持数据异步同步到多个Redis集群，无需担心性能问题
- **灵活读取**：可根据需要从指定机房的Redis实例读取数据
- **容错能力**：主Redis实例故障时可自动切换到备用实例
- **统一接口**：提供统一的API接口操作多个Redis实例，降低学习成本
- **性能优化**：针对多机房场景做了特殊的性能优化，使用虚拟线程和异步操作提高效率

## 核心功能与亮点 ✨

- **多模式支持**：支持单机、主从、集群、哨兵四种Redis模式
- **多实例管理**：同时支持多个Redis实例的连接和管理
- **自动配置**：与Spring Boot无缝整合，自动配置多个Redisson客户端和RedisTemplate
- **分离式配置**：Redisson客户端和RedisTemplate使用独立的配置类，职责清晰
- **统一操作工具**：提供`RedissonUtil`工具类，统一操作多个Redis实例
- **异步写入**：自动将写操作异步同步到备用Redis集群
- **Bean兼容性**：提供Master和Back两个主要Bean，保持向后兼容
- **线程池优化**：使用Java 21虚拟线程处理异步操作，提高性能和资源利用率
- **内存优化**：针对不同的机房集群参数进行了细致的内存和连接池优化

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- Redisson 3.x
- Transmittable Thread Local
- Netty

## 快速开始 🚀

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>multi-redis-spring-boot-starter</artifactId>
    <version>dev-2.4.6.5</version>
</dependency>
```

### 2. 配置文件

#### 基础配置

```yaml
multi:
  redis:
    enabled: true
    default-instance: default
    instances:
      default:
        enabled: true
        mode: SINGLE
        password: your_password
        single:
          address: localhost:6379
```

#### 单机模式配置

```yaml
multi:
  redis:
    enabled: true
    default-instance: cache
    instances:
      cache:
        enabled: true
        mode: SINGLE
        password: your_password
        database: 0
        connection-timeout: 5000
        response-timeout: 3000
        single:
          address: localhost:6379
          connection-pool-size: 64
          connection-minimum-idle-size: 24
```

#### 主从模式配置

```yaml
multi:
  redis:
    enabled: true
    default-instance: session
    instances:
      session:
        enabled: true
        mode: MASTER_SLAVE
        password: your_password
        database: 0
        master-slave:
          master-address: localhost:6379
          slave-addresses:
            - localhost:6380
            - localhost:6381
          master-connection-pool-size: 64
          slave-connection-pool-size: 64
          read-mode: SLAVE
```

#### 集群模式配置

```yaml
multi:
  redis:
    enabled: true
    default-instance: cluster
    instances:
      cluster:
        enabled: true
        mode: CLUSTER
        password: your_password
        cluster:
          node-addresses:
            - localhost:7000
            - localhost:7001
            - localhost:7002
            - localhost:7003
            - localhost:7004
            - localhost:7005
          master-connection-pool-size: 64
          slave-connection-pool-size: 64
          read-mode: SLAVE
          scan-interval: 5000
          check-slots-coverage: true
          max-redirects: 3
```

#### 哨兵模式配置

```yaml
multi:
  redis:
    enabled: true
    default-instance: sentinel
    instances:
      sentinel:
        enabled: true
        mode: SENTINEL
        password: your_password
        database: 0
        sentinel:
          master-name: mymaster
          sentinel-addresses:
            - localhost:26379
            - localhost:26380
            - localhost:26381
          master-connection-pool-size: 64
          slave-connection-pool-size: 64
          read-mode: SLAVE
          scan-interval: 1000
```

## 多套 Redis 配置示例 🔧

### 1. Master + Back + 其他实例配置

```yaml
multi:
  redis:
    enabled: true
    default-instance: master  # 默认使用 master 实例
    instances:
      # 主实例 - 作为 redissonClient Bean
      master:
        enabled: true
        mode: CLUSTER
        password: master_password
        cluster:
          node-addresses:
            - redis-master-1:7000
            - redis-master-2:7001
            - redis-master-3:7002
            - redis-master-4:7003
            - redis-master-5:7004
            - redis-master-6:7005
          master-connection-pool-size: 64
          slave-connection-pool-size: 64
          read-mode: SLAVE
      
      # 备用实例 - 作为 redissonClient2 Bean
      back:
        enabled: true
        mode: CLUSTER
        password: back_password
        cluster:
          node-addresses:
            - redis-back-1:7000
            - redis-back-2:7001
            - redis-back-3:7002
            - redis-back-4:7003
            - redis-back-5:7004
            - redis-back-6:7005
          master-connection-pool-size: 32
          slave-connection-pool-size: 32
          read-mode: SLAVE
      
      # 缓存实例 - 单机模式
      cache:
        enabled: true
        mode: SINGLE
        password: cache_password
        database: 0
        single:
          address: redis-cache:6379
          connection-pool-size: 32
          connection-minimum-idle-size: 10
      
      # 会话实例 - 主从模式
      session:
        enabled: true
        mode: MASTER_SLAVE
        password: session_password
        database: 1
        master-slave:
          master-address: redis-session-master:6379
          slave-addresses:
            - redis-session-slave1:6379
            - redis-session-slave2:6379
          master-connection-pool-size: 32
          slave-connection-pool-size: 32
          read-mode: SLAVE
      
      # 分布式锁实例 - 哨兵模式
      lock:
        enabled: true
        mode: SENTINEL
        password: lock_password
        database: 0
        sentinel:
          master-name: mymaster
          sentinel-addresses:
            - redis-sentinel-1:26379
            - redis-sentinel-2:26379
            - redis-sentinel-3:26379
          master-connection-pool-size: 16
          slave-connection-pool-size: 16
          read-mode: MASTER
```

### 2. 多机房配置示例

```yaml
multi:
  redis:
    enabled: true
    default-instance: beijing
    instances:
      # 北京机房 - 主实例
      beijing:
        enabled: true
        mode: CLUSTER
        password: beijing_redis_password
        connection-timeout: 3000
        response-timeout: 2000
        cluster:
          node-addresses:
            - beijing-redis-1:7000
            - beijing-redis-2:7001
            - beijing-redis-3:7002
            - beijing-redis-4:7003
            - beijing-redis-5:7004
            - beijing-redis-6:7005
          master-connection-pool-size: 64
          slave-connection-pool-size: 64
          read-mode: SLAVE
      
      # 上海机房 - 备用实例
      back:  # 这个会被注册为 redissonClient2
        enabled: true
        mode: CLUSTER
        password: shanghai_redis_password
        connection-timeout: 5000  # 跨机房延迟较高
        response-timeout: 3000
        cluster:
          node-addresses:
            - shanghai-redis-1:7000
            - shanghai-redis-2:7001
            - shanghai-redis-3:7002
            - shanghai-redis-4:7003
            - shanghai-redis-5:7004
            - shanghai-redis-6:7005
          master-connection-pool-size: 32
          slave-connection-pool-size: 32
          read-mode: SLAVE
      
      # 广州机房 - 其他实例
      guangzhou:
        enabled: true
        mode: CLUSTER
        password: guangzhou_redis_password
        connection-timeout: 5000
        response-timeout: 3000
        cluster:
          node-addresses:
            - guangzhou-redis-1:7000
            - guangzhou-redis-2:7001
            - guangzhou-redis-3:7002
```

### 3. 不同业务场景配置

```yaml
multi:
    redis:
    enabled: true
    default-instance: business
    instances:
      # 业务数据 - 主实例
      business:
        enabled: true
        mode: CLUSTER
        password: business_password
        cluster:
          node-addresses:
            - business-redis-1:7000
            - business-redis-2:7001
            - business-redis-3:7002
          master-connection-pool-size: 64
          slave-connection-pool-size: 64
          read-mode: SLAVE
      
      # 业务数据备份 - 备用实例
      back:
        enabled: true
        mode: CLUSTER
        password: business_backup_password
      cluster:
          node-addresses:
            - backup-redis-1:7000
            - backup-redis-2:7001
            - backup-redis-3:7002
          master-connection-pool-size: 32
          slave-connection-pool-size: 32
          read-mode: SLAVE
      
      # 用户会话
      user-session:
        enabled: true
        mode: SINGLE
        password: session_password
        database: 0
        single:
          address: session-redis:6379
          connection-pool-size: 32
      
      # 分布式锁
      distributed-lock:
        enabled: true
        mode: SENTINEL
        password: lock_password
        sentinel:
          master-name: lock-master
          sentinel-addresses:
            - lock-sentinel-1:26379
            - lock-sentinel-2:26379
            - lock-sentinel-3:26379
          master-connection-pool-size: 16
          slave-connection-pool-size: 16
      
      # 消息队列
      message-queue:
        enabled: true
        mode: MASTER_SLAVE
        password: mq_password
        master-slave:
          master-address: mq-redis-master:6379
          slave-addresses:
            - mq-redis-slave1:6379
            - mq-redis-slave2:6379
          master-connection-pool-size: 32
          slave-connection-pool-size: 32
          read-mode: MASTER  # 消息队列需要强一致性
      
      # 缓存数据
      cache-data:
        enabled: true
        mode: SINGLE
        password: cache_password
        database: 1
        single:
          address: cache-redis:6379
          connection-pool-size: 64
          connection-minimum-idle-size: 20
```

### 4. 环境配置示例

#### application-dev.yml (开发环境)
```yaml
multi:
  redis:
    enabled: true
    default-instance: dev
    instances:
      dev:
        enabled: true
        mode: SINGLE
        password: dev_password
        database: 0
        single:
          address: localhost:6379
          connection-pool-size: 16
      
      back:
        enabled: false  # 开发环境不需要备用实例
```

#### application-prod.yml (生产环境)
```yaml
multi:
  redis:
    enabled: true
    default-instance: prod-primary
    instances:
      prod-primary:
        enabled: true
        mode: CLUSTER
        password: ${REDIS_PRIMARY_PASSWORD}
        connection-timeout: 3000
        response-timeout: 2000
        retry-attempts: 3
        retry-interval: 1000
        netty-threads: 32
        cluster:
          node-addresses:
            - ${REDIS_PRIMARY_NODE1}:7000
            - ${REDIS_PRIMARY_NODE2}:7001
            - ${REDIS_PRIMARY_NODE3}:7002
            - ${REDIS_PRIMARY_NODE4}:7003
            - ${REDIS_PRIMARY_NODE5}:7004
            - ${REDIS_PRIMARY_NODE6}:7005
          master-connection-pool-size: 64
          slave-connection-pool-size: 64
          read-mode: SLAVE
          scan-interval: 5000
          check-slots-coverage: true
      
      back:
        enabled: true
        mode: CLUSTER
        password: ${REDIS_BACKUP_PASSWORD}
        connection-timeout: 5000
        response-timeout: 3000
        cluster:
          node-addresses:
            - ${REDIS_BACKUP_NODE1}:7000
            - ${REDIS_BACKUP_NODE2}:7001
            - ${REDIS_BACKUP_NODE3}:7002
            - ${REDIS_BACKUP_NODE4}:7003
            - ${REDIS_BACKUP_NODE5}:7004
            - ${REDIS_BACKUP_NODE6}:7005
          master-connection-pool-size: 32
          slave-connection-pool-size: 32
          read-mode: SLAVE
```

### 3. 使用方式

#### 注入客户端管理器

```java
@Service
public class UserService {

    @Autowired
    private MultiRedisClientManager redisClientManager;
    
    @Autowired
    private RedissonUtil redissonUtil;
    
    public void cacheUser(User user) {
        // 使用默认实例（master）
        redissonUtil.set("user:" + user.getId(), user);
        
        // 使用指定实例
        redissonUtil.set("cache", "user:" + user.getId(), user);
        
        // 直接获取客户端
        RedissonClient cacheClient = redisClientManager.getClient("cache");
        cacheClient.getBucket("user:" + user.getId()).set(user);
    }
}
```

#### 使用工具类 - 兼容原有功能

```java
@Service
public class CacheService {

    @Autowired
    private RedissonUtil redissonUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 字符串操作 - 原有方法保持不变
    public void stringOperations() {
        // 使用默认实例（会自动同步到 back 实例）
        redissonUtil.set("key", "value");
        String value = redissonUtil.get("key");
        
        // 使用指定实例
        redissonUtil.set("cache", "key", "value", Duration.ofMinutes(10));
        String value2 = redissonUtil.get("cache", "key");
    }
    
    // RedisTemplate 操作 - 基于默认实例
    public void redisTemplateOperations() {
        // 字符串操作
        redisTemplate.opsForValue().set("template:key", "template:value");
        redisTemplate.opsForValue().set("template:key2", "template:value2", Duration.ofMinutes(10));
        String value = (String) redisTemplate.opsForValue().get("template:key");
        
        // 哈希操作
        redisTemplate.opsForHash().put("template:hash", "field1", "value1");
        redisTemplate.opsForHash().put("template:hash", "field2", "value2");
        Object hashValue = redisTemplate.opsForHash().get("template:hash", "field1");
        Map<Object, Object> allHash = redisTemplate.opsForHash().entries("template:hash");
        
        // 列表操作
        redisTemplate.opsForList().leftPush("template:list", "item1");
        redisTemplate.opsForList().leftPush("template:list", "item2");
        List<Object> listItems = redisTemplate.opsForList().range("template:list", 0, -1);
    
        // 集合操作
        redisTemplate.opsForSet().add("template:set", "member1", "member2", "member3");
        Set<Object> setMembers = redisTemplate.opsForSet().members("template:set");
        
        // 有序集合操作
        redisTemplate.opsForZSet().add("template:zset", "member1", 1.0);
        redisTemplate.opsForZSet().add("template:zset", "member2", 2.0);
        Set<Object> zsetMembers = redisTemplate.opsForZSet().range("template:zset", 0, -1);
        
        // 过期时间操作
        redisTemplate.expire("template:key", Duration.ofMinutes(30));
        Boolean hasKey = redisTemplate.hasKey("template:key");
        Long ttl = redisTemplate.getExpire("template:key");
        
        // 删除操作
        redisTemplate.delete("template:key");
        redisTemplate.delete(Arrays.asList("template:key1", "template:key2"));
    }
    
    // 哈希操作 - 支持多实例
    public void hashOperations() {
        // 使用默认实例
        redissonUtil.hset("hash_key", "field", "value");
        
        // 使用指定实例
        redissonUtil.hset("session", "hash_key", "field", "value");
    }
    
    // 锁操作 - 支持指定实例
    public void lockOperations() {
        // 使用默认实例的锁
        RLock lock = redissonUtil.getLock("lock_key");
        
        // 使用分布式锁实例
        RLock distributedLock = redissonUtil.getLock("distributed-lock", "business_lock");
        
        try {
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                // 业务逻辑
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
```

#### 获取特定实例的客户端

```java
@Service
public class DistributedLockService {

    @Autowired
    private MultiRedisClientManager redisClientManager;
    
    public boolean tryLock(String lockKey) {
        // 获取专门用于分布式锁的 Redis 实例
        RedissonClient lockClient = redisClientManager.getClient("distributed-lock");
        RLock lock = lockClient.getLock(lockKey);
        
        try {
            return lock.tryLock(10, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
```

#### 兼容原有代码

```java
@Service
public class LegacyService {
    
    @Autowired
    @Qualifier("redissonClient")
    private RedissonClient redissonClient;  // 主实例
    
    @Autowired
    @Qualifier("redissonClient2")
    private RedissonClient redissonClient2; // 备用实例（可能为null）
    
    @Autowired
    private RedissonUtil redissonUtil;
    
    public void legacyMethod() {
        // 原有代码无需修改，继续使用
        redissonClient.getBucket("key").set("value");
        
        // RedissonUtil 的原有方法也保持不变
        redissonUtil.set("key", "value");  // 会自动同步到备用实例
        redissonUtil.hset("hash", "field", "value");
    }
}
```

## Bean 说明 📦

### 自动创建的 Bean

1. **redissonClient** (Primary Bean)
   - 对应 `default-instance` 配置的实例
   - 作为主要的 Redis 客户端

2. **redissonClient2** (可选 Bean)
   - 对应名为 "back" 或 "backup" 的实例
   - 作为备用 Redis 客户端
   - 如果没有配置则为 null

3. **redisTemplate** (Primary Bean)
   - 基于 `default-instance` 配置的 RedisTemplate
   - 使用 String 序列化 key，JSON 序列化 value
   - 支持标准的 Spring Data Redis 操作

4. **redisConnectionFactory** (Primary Bean)
   - 基于 `default-instance` 配置的连接工厂
   - 支持单机、主从、集群、哨兵四种模式

5. **MultiRedisClientManager**
   - 管理所有 Redis 实例
   - 包含 master、back 和其他所有实例

### Bean 注入示例

```java
@Component
public class RedisService {
    
    // 主实例 - 自动注入
    @Autowired
    private RedissonClient redissonClient;
    
    // 备用实例 - 可能为 null
    @Autowired(required = false)
    private RedissonClient redissonClient2;
    
    // RedisTemplate - 基于默认实例
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 实例管理器
    @Autowired
    private MultiRedisClientManager redisClientManager;
    
    // 工具类
    @Autowired
    private RedissonUtil redissonUtil;
    
    public void useRedis() {
        // 使用主实例
        redissonClient.getBucket("key1").set("value1");
        
        // 使用备用实例（如果存在）
        if (redissonClient2 != null) {
            redissonClient2.getBucket("key2").set("value2");
            }
            
        // 使用 RedisTemplate（推荐用于简单操作）
        redisTemplate.opsForValue().set("template_key", "template_value");
        String value = (String) redisTemplate.opsForValue().get("template_key");
        
        // 使用指定实例
        RedissonClient cacheClient = redisClientManager.getClient("cache");
        cacheClient.getBucket("cache_key").set("cache_value");
        
        // 使用工具类（推荐用于复杂操作）
        redissonUtil.set("key3", "value3");  // 使用默认实例
        redissonUtil.set("cache", "key4", "value4");  // 使用指定实例
    }
}
```

## 配置参数说明

### 全局配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `multi.redis.enabled` | boolean | false | 是否启用多 Redis 配置 |
| `multi.redis.default-instance` | String | default | 默认实例名称，对应 redissonClient Bean |

### 实例配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enabled` | boolean | true | 是否启用此实例 |
| `mode` | enum | SINGLE | Redis 模式：SINGLE/MASTER_SLAVE/CLUSTER/SENTINEL |
| `password` | String | - | Redis 密码 |
| `database` | int | 0 | 数据库索引（仅单机和主从模式） |
| `connection-timeout` | int | 5000 | 连接超时时间（毫秒） |
| `response-timeout` | int | 3000 | 响应超时时间（毫秒） |
| `idle-connection-timeout` | int | 10000 | 空闲连接超时时间（毫秒） |
| `retry-attempts` | int | 3 | 重试次数 |
| `retry-interval` | int | 1000 | 重试间隔（毫秒） |
| `netty-threads` | int | 32 | Netty 线程数 |

### 单机模式配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `single.address` | String | - | Redis 服务器地址 |
| `single.connection-pool-size` | int | 64 | 连接池大小 |
| `single.connection-minimum-idle-size` | int | 24 | 最小空闲连接数 |

### 主从模式配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `master-slave.master-address` | String | - | 主节点地址 |
| `master-slave.slave-addresses` | List<String> | - | 从节点地址列表 |
| `master-slave.read-mode` | enum | SLAVE | 读取模式 |

### 集群模式配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `cluster.node-addresses` | List<String> | - | 集群节点地址列表 |
| `cluster.scan-interval` | int | 5000 | 集群扫描间隔（毫秒） |
| `cluster.check-slots-coverage` | boolean | true | 是否检查槽位覆盖 |
| `cluster.max-redirects` | int | 3 | 最大重定向次数 |

### 哨兵模式配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `sentinel.master-name` | String | - | 主服务器名称 |
| `sentinel.sentinel-addresses` | List<String> | - | 哨兵节点地址列表 |
| `sentinel.scan-interval` | int | 1000 | 哨兵扫描间隔（毫秒） |

## 特殊实例名称说明 🏷️

### 预定义实例名称

- **default-instance**: 配置的默认实例名称，会被注册为 `redissonClient` Bean
- **back**: 会被注册为 `redissonClient2` Bean，用作备用实例
- **backup**: 如果没有 "back"，会查找 "backup" 作为 `redissonClient2` Bean

### 实例优先级

1. `redissonClient` = `default-instance` 配置的实例
2. `redissonClient2` = "back" > "backup" > null
3. 所有实例都会注册到 `MultiRedisClientManager`

## 注意事项

1. **实例命名**: 
   - `default-instance` 指定的实例作为主实例
   - 名为 "back" 或 "backup" 的实例作为备用实例
   - 其他实例通过 `MultiRedisClientManager` 访问

2. **兼容性保证**:
   - 保留原有的 `writeWithResult` 和 `write` 方法
   - 支持主备自动同步功能
   - 原有代码无需修改
   - 提供标准的 `RedisTemplate` Bean，基于默认实例

3. **配置要求**:
   - 确保配置的 Redis 服务器地址可访问
   - 集群模式需要至少 3 个主节点
   - 哨兵模式需要至少 3 个哨兵节点
   - 密码配置为可选

4. **性能考虑**:
   - 跨机房实例建议增加超时时间
   - 根据业务需求调整连接池大小
   - 备用实例可以使用较小的连接池

## 版本兼容性

- Spring Boot 3.x
- Java 21+
- Redisson 3.x

## 更新日志

### v2.4.6.5
- 重构多 Redis 实例配置
- 支持四种 Redis 模式（单机、主从、集群、哨兵）
- 提供统一的客户端管理器
- 更新工具类支持实例级别操作
- 保持向后兼容性，支持原有 Bean 注入方式
- 新增 Master/Back Bean 自动配置
- 完善多套 Redis 配置支持
- 新增 RedisTemplate Bean 自动配置，基于默认实例
- **配置类分离**：将 RedisTemplate 配置独立为 `RedisTemplateAutoConfiguration`，与 Redisson 配置分离，职责更清晰

## 应用场景

- **多区域部署**：应用部署在多个地区，需要将数据同步到各个区域的Redis
- **数据容灾备份**：将数据同时写入多个Redis集群，实现灾备
- **就近访问加速**：用户访问就近的Redis集群，降低延迟
- **读写分离**：主Redis实例负责写操作，从Redis实例负责读操作
- **业务隔离**：不同业务使用不同的Redis实例，避免相互影响
- **灰度发布**：新功能先在一个Redis集群上测试，确认无误后再推广
- **流量分担**：将不同类型的数据存储在不同的Redis集群，分担负载

## 常见问题

### 1. 如何配置多套Redis？

参考上面的"多套 Redis 配置示例"部分，根据您的需求选择合适的配置方式。

### 2. 如何保持向后兼容？

组件自动创建 `redissonClient`、`redissonClient2` 和 `redisTemplate` Bean，原有代码无需修改：

```java
// 原有代码继续有效
@Autowired
private RedissonClient redissonClient;

@Autowired
private RedissonUtil redissonUtil;

// 新增的 RedisTemplate 支持
@Autowired
private RedisTemplate<String, Object> redisTemplate;
```

### 3. RedisTemplate 和 RedissonClient 有什么区别？

- **RedisTemplate**: Spring Data Redis 的标准模板，适合简单的 CRUD 操作
- **RedissonClient**: Redisson 客户端，提供更丰富的分布式功能（锁、队列、布隆过滤器等）

推荐使用场景：
- 简单缓存操作：使用 `RedisTemplate`
- 分布式锁、队列等高级功能：使用 `RedissonClient` 或 `RedissonUtil`

### 4. 多Redis实例同步失败怎么办？

当异步同步到备用Redis失败时，系统会记录错误日志但不会影响主流程。建议：

1. 实现自定义的重试机制
2. 使用消息队列进行数据同步
3. 定期进行数据校验和修复

### 5. 如何监控多个Redis实例？

建议设置以下监控指标：

1. 各Redis实例的可用性
2. 数据同步的成功率和延迟
3. 各实例的负载情况
4. 异步操作线程池的使用情况

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description Redis监控服务
 */
@Service
@Slf4j
public class RedisMonitorService {

    @Autowired
    private MultiRedisClientManager redisClientManager;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 检查所有Redis实例可用性
     */
    @Scheduled(fixedRate = 60000)  // 每分钟执行一次
    public void checkRedisAvailability() {
        Map<String, Boolean> status = new HashMap<>();
        
        // 检查默认实例（RedisTemplate）
        try {
            redisTemplate.opsForValue().set("health:check:template", "ok");
            status.put("default-template", true);
        } catch (Exception e) {
            log.error("RedisTemplate 不可用", e);
            status.put("default-template", false);
        }
        
        // 检查所有 Redisson 实例
        for (String instanceName : redisClientManager.getInstanceNames()) {
        try {
                RedissonClient client = redisClientManager.getClient(instanceName);
                client.getBucket("health:check:" + instanceName).set("ok");
                status.put(instanceName, true);
        } catch (Exception e) {
                log.error("Redis实例不可用: {}", instanceName, e);
                status.put(instanceName, false);
            }
        }
        
        log.info("Redis实例状态: {}", status);
    }
}
```

### 6. 如何处理Redis配置变更？

Redis配置变更通常需要重启应用，但可以实现动态刷新机制：

1. 使用Spring Cloud Config或其他配置中心动态更新配置
2. 实现自定义的Redis连接池管理器，支持动态刷新
3. 使用监听器监听配置变更，重新初始化Redis连接

### 7. 配置类的职责分工是什么？

项目采用分离式配置设计，职责清晰：

- **`MultiRedissonConfig`**: 负责 Redisson 客户端的配置和管理
  - 创建 `MultiRedisClientManager`
  - 创建 `redissonClient` 和 `redissonClient2` Bean
  - 管理多个 Redisson 实例

- **`RedisTemplateAutoConfiguration`**: 负责 RedisTemplate 的配置
  - 创建 `RedisConnectionFactory`
  - 创建 `RedisTemplate` Bean
  - 基于默认实例配置连接工厂

这种设计的优势：
- 职责单一，便于维护
- 可以独立扩展和修改
- 降低配置类的复杂度
- 便于单元测试

## 贡献 🙏

欢迎提交Issue或Pull Request参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。

## 致谢

本项目基于 [Redisson](https://github.com/redisson/redisson) 开发，感谢 Redisson 项目团队提供的优秀分布式工具。 