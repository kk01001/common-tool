# Multi Redisson Spring Boot Starter

Redisson 多集群自动配置组件，支持一个 Spring Boot 服务连接多套 Redis，支持单机、哨兵、主从、集群四种模式。

## 功能特性

- ✅ **多实例支持**：支持配置任意数量的 Redis 实例
- ✅ **多模式支持**：支持单机（Single）、哨兵（Sentinel）、主从（Master-Slave）、集群（Cluster）四种模式
- ✅ **双写支持**：支持双机房双写，备份集群异步写入
- ✅ **完整配置**：覆盖 Redisson 所有常用配置项
- ✅ **灵活注入**：支持 `@Primary`、`@Qualifier`、`RedissonClientHolder` 三种方式获取客户端
- ✅ **统一操作模板**：`MultiRedissonTemplate` 封装双写逻辑，对业务透明
- ✅ **生命周期管理**：自动管理客户端的创建和销毁
- ✅ **SSL 支持**：支持 SSL/TLS 安全连接
- ✅ **EPOLL 支持**：Linux 环境自动启用 EPOLL 提升性能

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>multi-redisson-spring-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```

### 2. 配置文件

#### 基础多实例配置

```yaml
redisson:
  multi:
    enabled: true
    # 主实例名称
    primary: master
    # 多个 Redis 实例配置
    instances:
      # 实例1：单机模式
      master:
        mode: single
        single:
          address: redis://127.0.0.1:6379
          password: your_password
          database: 0
          connectionPoolSize: 64
          connectionMinimumIdleSize: 24
          connectTimeout: 10000
          timeout: 3000
        threads: 16
        nettyThreads: 32
        codec: org.redisson.client.codec.StringCodec
        
      # 实例2：集群模式
      cluster-redis:
        mode: cluster
        cluster:
          nodeAddresses:
            - redis://192.168.1.1:7001
            - redis://192.168.1.1:7002
            - redis://192.168.1.2:7001
          password: cluster_password
          scanInterval: 5000
          readMode: SLAVE
          masterConnectionPoolSize: 64
          slaveConnectionPoolSize: 64
          
      # 实例3：哨兵模式
      sentinel-redis:
        mode: sentinel
        sentinel:
          masterName: mymaster
          sentinelAddresses:
            - redis://192.168.2.1:26379
            - redis://192.168.2.2:26379
          password: sentinel_password
          database: 0
          readMode: SLAVE
          
      # 实例4：主从模式
      master-slave-redis:
        mode: master-slave
        masterSlave:
          masterAddress: redis://192.168.3.1:6379
          slaveAddresses:
            - redis://192.168.3.2:6379
            - redis://192.168.3.3:6379
          password: ms_password
          database: 0
```

#### 双机房双写配置

```yaml
redisson:
  multi:
    enabled: true
    # 主集群（机房A）
    primary: dc-a
    # 备份集群（机房B）
    secondary: dc-b
    # 启用双写
    dual-write-enabled: true
    # 双写线程池配置
    dual-write-thread-pool:
      core-pool-size: 4
      max-pool-size: 8
      queue-capacity: 10000
      keep-alive-seconds: 60
      thread-name-prefix: dual-write-
      allow-core-thread-time-out: false
    
    instances:
      # 机房A - 主集群
      dc-a:
        mode: cluster
        cluster:
          nodeAddresses:
            - redis://192.168.1.1:7001
            - redis://192.168.1.1:7002
            - redis://192.168.1.2:7001
            - redis://192.168.1.2:7002
          password: dc_a_password
          scanInterval: 5000
          readMode: SLAVE
          masterConnectionPoolSize: 64
          slaveConnectionPoolSize: 64
        threads: 16
        nettyThreads: 32
        
      # 机房B - 备份集群
      dc-b:
        mode: cluster
        cluster:
          nodeAddresses:
            - redis://192.168.2.1:7001
            - redis://192.168.2.1:7002
            - redis://192.168.2.2:7001
            - redis://192.168.2.2:7002
          password: dc_b_password
          scanInterval: 5000
          readMode: SLAVE
          masterConnectionPoolSize: 64
          slaveConnectionPoolSize: 64
        threads: 16
        nettyThreads: 32
```

### 3. 使用方式

#### 方式一：直接使用 RedissonClient

```java
@Service
public class RedisService {
    
    // 注入主实例（@Primary）
    @Autowired
    private RedissonClient redissonClient;
    
    // 通过 @Qualifier 注入指定实例
    @Autowired
    @Qualifier("cluster-redisRedissonClient")
    private RedissonClient clusterRedissonClient;
    
    // 通过 Holder 动态获取
    @Autowired
    private RedissonClientHolder redissonClientHolder;
    
    public void demo() {
        // 使用主实例
        RBucket<String> bucket = redissonClient.getBucket("key");
        bucket.set("value");
        
        // 使用指定实例
        RBucket<String> clusterBucket = clusterRedissonClient.getBucket("key");
        
        // 通过 Holder 动态获取
        RedissonClient sentinelClient = redissonClientHolder.getClient("sentinel-redis");
        RBucket<String> sentinelBucket = sentinelClient.getBucket("key");
    }
}
```

#### 方式二：使用 MultiRedissonTemplate（推荐，支持双写）

```java
@Service
public class RedisService {
    
    @Autowired
    private MultiRedissonTemplate redissonTemplate;
    
    public void demo() {
        // ========== String 操作 ==========
        // 设置值（自动双写）
        redissonTemplate.set("user:1", "张三");
        redissonTemplate.set("user:2", "李四", Duration.ofMinutes(30));
        
        // 获取值（只从主集群读取）
        String name = redissonTemplate.get("user:1");
        
        // ========== Set 操作 ==========
        // 添加元素（双写）
        redissonTemplate.sadd("tags", "java", "redis", "spring");
        
        // 随机弹出元素（主集群随机弹出，备份集群删除指定元素）
        String tag = redissonTemplate.spop("tags");
        
        // ========== Hash 操作 ==========
        redissonTemplate.hset("user:info", "name", "张三");
        redissonTemplate.hset("user:info", "age", 25);
        Map<String, Object> userInfo = redissonTemplate.hgetAll("user:info");
        
        // ========== ZSet 操作 ==========
        redissonTemplate.zadd("ranking", "player1", 100.0);
        redissonTemplate.zadd("ranking", "player2", 95.0);
        
        // 弹出分数最高的（主集群弹出，备份集群删除该元素）
        String topPlayer = redissonTemplate.zpopMax("ranking");
        
        // ========== 原子操作 ==========
        long count = redissonTemplate.increment("counter", Duration.ofHours(1));
        
        // ========== 分布式锁（只在主集群） ==========
        RLock lock = redissonTemplate.getLock("lock:order:123");
        try {
            if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                // 业务逻辑
            }
        } finally {
            lock.unlock();
        }
    }
}
```

## 双写特性说明

### 工作原理

1. **主集群**：同步执行所有操作，结果立即返回
2. **备份集群**：通过线程池异步执行写操作，不影响主流程
3. **读操作**：只从主集群读取

### 特殊操作处理

| 操作 | 主集群行为 | 备份集群行为 |
|------|-----------|-------------|
| `spop` | 随机弹出元素 | 删除主集群弹出的指定元素 |
| `spop(count)` | 随机弹出多个元素 | 删除主集群弹出的所有元素 |
| `zpopMax/zpopMin` | 弹出分数最高/最低元素 | 删除该指定元素 |
| `pollFirst/pollLast` | 弹出队首/队尾元素 | 删除该指定元素 |
| `takeBlockingQueue` | 阻塞取出元素 | 删除该指定元素 |

### 双写失败处理

- 备份集群写入失败不影响主流程
- 失败会记录错误日志，可配合监控告警
- 线程池满时使用 `CallerRunsPolicy`，由调用线程执行

## 配置说明

### 通用配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `redisson.multi.enabled` | 是否启用多集群配置 | `false` |
| `redisson.multi.primary` | 主实例名称 | `default` |
| `redisson.multi.secondary` | 备份实例名称（双写用） | - |
| `redisson.multi.dual-write-enabled` | 是否启用双写 | `false` |

### 双写线程池配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `dual-write-thread-pool.core-pool-size` | 核心线程数 | `4` |
| `dual-write-thread-pool.max-pool-size` | 最大线程数 | `8` |
| `dual-write-thread-pool.queue-capacity` | 队列容量 | `10000` |
| `dual-write-thread-pool.keep-alive-seconds` | 线程空闲时间（秒） | `60` |
| `dual-write-thread-pool.thread-name-prefix` | 线程名称前缀 | `dual-write-` |
| `dual-write-thread-pool.allow-core-thread-time-out` | 是否允许核心线程超时 | `false` |

### 实例通用配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `mode` | 部署模式：SINGLE/SENTINEL/MASTER_SLAVE/CLUSTER | `SINGLE` |
| `threads` | Redis 连接线程数 | `16` |
| `nettyThreads` | Netty 线程数 | `32` |
| `codec` | 编解码器类名 | `StringCodec` |
| `transportMode` | 传输模式：NIO/EPOLL/KQUEUE | `NIO` |
| `checkLockSyncedSlaves` | 是否检查锁的同步从节点 | `true` |
| `slavesSyncTimeout` | 从节点同步超时（毫秒） | `1000` |
| `lockWatchdogTimeout` | 锁看门狗超时（毫秒） | `30000` |

### 单机模式配置 (single)

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `address` | Redis 地址 | - |
| `password` | 密码 | - |
| `database` | 数据库索引 | `0` |
| `connectionPoolSize` | 连接池大小 | `64` |
| `connectionMinimumIdleSize` | 最小空闲连接数 | `24` |
| `connectTimeout` | 连接超时（毫秒） | `10000` |
| `timeout` | 命令超时（毫秒） | `3000` |
| `retryAttempts` | 重试次数 | `3` |
| `retryInterval` | 重试间隔（毫秒） | `1500` |

### 哨兵模式配置 (sentinel)

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `masterName` | 主节点名称 | - |
| `sentinelAddresses` | 哨兵地址列表 | - |
| `password` | Redis 密码 | - |
| `sentinelPassword` | 哨兵密码 | - |
| `database` | 数据库索引 | `0` |
| `readMode` | 读取模式：SLAVE/MASTER/MASTER_SLAVE | `SLAVE` |
| `masterConnectionPoolSize` | Master 连接池大小 | `64` |
| `slaveConnectionPoolSize` | Slave 连接池大小 | `64` |
| `scanInterval` | 哨兵扫描间隔（毫秒） | `1000` |

### 主从模式配置 (masterSlave)

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `masterAddress` | 主节点地址 | - |
| `slaveAddresses` | 从节点地址列表 | - |
| `password` | 密码 | - |
| `database` | 数据库索引 | `0` |
| `readMode` | 读取模式：SLAVE/MASTER/MASTER_SLAVE | `SLAVE` |
| `masterConnectionPoolSize` | Master 连接池大小 | `64` |
| `slaveConnectionPoolSize` | Slave 连接池大小 | `64` |

### 集群模式配置 (cluster)

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `nodeAddresses` | 集群节点地址列表 | - |
| `password` | 密码 | - |
| `scanInterval` | 集群扫描间隔（毫秒） | `5000` |
| `readMode` | 读取模式：SLAVE/MASTER/MASTER_SLAVE | `SLAVE` |
| `masterConnectionPoolSize` | Master 连接池大小 | `64` |
| `slaveConnectionPoolSize` | Slave 连接池大小 | `64` |
| `checkSlotsCoverage` | 是否检查槽位覆盖 | `true` |
| `natMapper` | NAT 地址映射 | - |

## MultiRedissonTemplate API

### String 操作

| 方法 | 说明 | 双写 |
|------|------|------|
| `set(key, value)` | 设置值 | ✅ |
| `set(key, value, duration)` | 设置值和过期时间 | ✅ |
| `setNx(key, value, duration)` | 不存在时设置 | ✅ |
| `get(key)` | 获取值 | ❌ |
| `getAndSet(key, value)` | 获取并设置新值 | ✅ |
| `getAndDelete(key)` | 获取并删除 | ✅ |

### Hash 操作

| 方法 | 说明 | 双写 |
|------|------|------|
| `hset(key, field, value)` | 设置字段 | ✅ |
| `hmset(key, map)` | 批量设置字段 | ✅ |
| `hget(key, field)` | 获取字段值 | ❌ |
| `hgetAll(key)` | 获取所有字段 | ❌ |
| `hdel(key, fields...)` | 删除字段 | ✅ |
| `hincrby(key, field, value)` | 字段递增 | ✅ |

### Set 操作

| 方法 | 说明 | 双写 |
|------|------|------|
| `sadd(key, values...)` | 添加元素 | ✅ |
| `srem(key, values...)` | 移除元素 | ✅ |
| `smembers(key)` | 获取所有元素 | ❌ |
| `sismember(key, value)` | 判断元素是否存在 | ❌ |
| `spop(key)` | 随机弹出元素 | ✅ (特殊) |
| `spop(key, count)` | 随机弹出多个元素 | ✅ (特殊) |

### ZSet 操作

| 方法 | 说明 | 双写 |
|------|------|------|
| `zadd(key, value, score)` | 添加元素 | ✅ |
| `zrem(key, values...)` | 移除元素 | ✅ |
| `zincrby(key, value, delta)` | 增加分数 | ✅ |
| `zpopMax(key)` | 弹出最高分元素 | ✅ (特殊) |
| `zpopMin(key)` | 弹出最低分元素 | ✅ (特殊) |
| `zrangeByScore(key, min, max)` | 按分数范围查询 | ❌ |

### 通用操作

| 方法 | 说明 | 双写 |
|------|------|------|
| `delete(key)` | 删除 key | ✅ |
| `delete(keys)` | 批量删除 | ✅ |
| `expire(key, duration)` | 设置过期时间 | ✅ |
| `exists(key)` | 判断是否存在 | ❌ |
| `getExpire(key)` | 获取过期时间 | ❌ |

## RedissonClientHolder API

| 方法 | 说明 |
|------|------|
| `getPrimary()` | 获取主 RedissonClient |
| `getClient(String name)` | 根据名称获取客户端 |
| `getClientOrNull(String name)` | 安全获取客户端（不存在返回 null） |
| `hasClient(String name)` | 检查客户端是否存在 |
| `getClientNames()` | 获取所有客户端名称 |
| `getAllClients()` | 获取所有客户端 |
| `registerClient(String name, RedissonClient client)` | 动态注册客户端 |
| `removeClient(String name)` | 移除并关闭客户端 |

## 监控端点

启用 Actuator 后，可通过 `/actuator/dualwrite` 端点查看双写监控信息。

### 配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: dualwrite,health,info
```

### 端点接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/actuator/dualwrite` | 获取所有监控信息 |
| GET | `/actuator/dualwrite/overview` | 获取统计概览 |
| GET | `/actuator/dualwrite/threadPool` | 获取线程池状态 |
| GET | `/actuator/dualwrite/operations` | 获取各操作统计 |
| DELETE | `/actuator/dualwrite` | 重置统计数据 |

### 响应示例

```json
{
  "timestamp": "2026-01-15T16:30:00",
  "overview": {
    "totalSubmitted": 10000,
    "totalExecuted": 9998,
    "totalSuccess": 9990,
    "totalFailure": 8,
    "submitFailure": 2,
    "successRate": "99.92%",
    "failureRate": "0.08%"
  },
  "threadPool": {
    "corePoolSize": 4,
    "maxPoolSize": 8,
    "currentPoolSize": 4,
    "activeCount": 2,
    "largestPoolSize": 6,
    "completedTaskCount": 9998,
    "totalTaskCount": 10000,
    "poolUsageRate": "25.00%",
    "queue": {
      "size": 0,
      "remainingCapacity": 10000,
      "usageRate": "0.00%"
    }
  },
  "operations": {
    "success": {
      "set": 5000,
      "hset": 2000,
      "sadd": 1500,
      "zadd": 1490
    },
    "failure": {
      "set": 3,
      "hset": 2,
      "sadd": 3
    }
  }
}
```

### 监控指标说明

| 指标 | 说明 |
|------|------|
| `totalSubmitted` | 总提交任务数 |
| `totalExecuted` | 总执行任务数（成功+失败） |
| `totalSuccess` | 成功次数 |
| `totalFailure` | 执行失败次数 |
| `submitFailure` | 提交失败次数（线程池拒绝） |
| `successRate` | 成功率 |
| `failureRate` | 失败率 |
| `poolUsageRate` | 线程池使用率 |
| `queueUsageRate` | 队列使用率 |

## 注意事项

1. **地址格式**：Redis 地址需要包含协议前缀，如 `redis://host:port` 或 `rediss://host:port`（SSL）
2. **主实例**：`primary` 配置的实例会作为 `@Primary` Bean，可直接 `@Autowired` 注入
3. **Bean 名称**：每个实例会注册为 `{name}RedissonClient` 的 Bean，如 `masterRedissonClient`
4. **生命周期**：Spring 容器关闭时会自动关闭所有 RedissonClient 连接
5. **EPOLL**：Linux 环境下建议设置 `transportMode: EPOLL` 或 `AUTO` 以获得更好的性能

## License

Apache License 2.0
