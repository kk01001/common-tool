# 多Redis实例 Spring Boot Starter

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

- **多集群支持**：同时支持最多三个Redis集群的连接和管理
- **自动配置**：与Spring Boot无缝整合，自动配置多个Redisson客户端
- **统一操作工具**：提供`RedissonUtil`工具类，统一操作多个Redis实例
- **异步写入**：自动将写操作异步同步到备用Redis集群
- **机房位置标识**：通过配置标识不同Redis集群的机房位置，便于业务逻辑选择
- **线程池优化**：使用Java 21虚拟线程处理异步操作，提高性能和资源利用率
- **内存优化**：针对不同的机房集群参数进行了细致的内存和连接池优化

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- Redisson 3.x
- Transmittable Thread Local
- Netty

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>multi-redis-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 配置多Redis集群

在 `application.yml` 或 `application.properties` 中配置多个Redis集群：

```yaml
spring:
  data:
    redis:
      # 通用配置
      password: common-password  # 可选，如果所有集群密码相同
      connection-timeout: 5000   # 连接超时时间
      response-timeout: 3000     # 响应超时时间
      master-connection-pool-size: 100  # 主节点连接池大小
      slave-connection-pool-size: 100   # 从节点连接池大小
      
      # 主集群配置
      cluster:
        active: true             # 是否启用
        location: A              # 机房位置标识
        password: clusterA-pass  # 集群密码，覆盖通用密码
        nodes:                   # 节点列表
          - 192.168.1.1:6379
          - 192.168.1.2:6379
          - 192.168.1.3:6379
        netty-threads: 32        # Netty线程数
        max-redirects: 3         # 最大重定向次数
      
      # 备用集群配置
      cluster2:
        active: true             # 是否启用
        location: B              # 机房位置标识
        password: clusterB-pass  # 集群密码
        nodes:                   # 节点列表
          - 192.168.2.1:6379
          - 192.168.2.2:6379
          - 192.168.2.3:6379
        netty-threads: 16        # Netty线程数
```

### 使用RedissonUtil操作Redis

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final RedissonUtil redissonUtil;
    
    /**
     * 保存用户数据到缓存
     * 自动写入所有配置的Redis集群
     */
    public void cacheUserInfo(String userId, UserDTO userInfo) {
        String key = "user:info:" + userId;
        // 写入数据，自动同步到所有激活的Redis集群
        redissonUtil.setSerialize(key, userInfo);
        // 设置过期时间
        redissonUtil.setExpire(key, Duration.ofHours(2));
    }
    
    /**
     * 从缓存获取用户数据
     */
    public UserDTO getUserInfo(String userId) {
        String key = "user:info:" + userId;
        String json = redissonUtil.get(key);
        if (json != null) {
            return objectMapper.readValue(json, UserDTO.class);
        }
        return null;
    }
    
    /**
     * 从指定机房获取数据
     */
    public UserDTO getUserInfoFromLocation(String userId, String location) {
        String key = "user:info:" + userId;
        RedissonClient client = redissonUtil.getRedissonClient(location);
        String json = client.getBucket(key).get();
        if (json != null) {
            return objectMapper.readValue(json, UserDTO.class);
        }
        return null;
    }
}
```

## 高级用法

### 1. 操作Hash数据结构

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 产品服务
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final RedissonUtil redissonUtil;
    
    /**
     * 批量更新产品库存
     */
    public void updateStocks(Map<String, Integer> productStocks) {
        String key = "product:stocks";
        // 批量设置HashMap
        redissonUtil.setHash(key, productStocks);
    }
    
    /**
     * 更新单个产品库存
     */
    public void updateStock(String productId, int stock) {
        String key = "product:stocks";
        // 设置单个Hash字段
        redissonUtil.setHash(key, productId, stock);
    }
    
    /**
     * 获取产品库存
     */
    public Integer getStock(String productId) {
        String key = "product:stocks";
        // 获取Hash字段值
        return redissonUtil.getHashByItem(key, productId);
    }
    
    /**
     * 递增产品销量
     */
    public Long incrementSales(String productId, long increment) {
        String key = "product:sales";
        // 递增Hash字段值并设置过期时间
        return redissonUtil.hashIncrement(key, productId, increment, Duration.ofDays(30));
    }
}
```

### 2. 使用有序集合(ZSet)实现排行榜

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 排行榜服务
 */
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final RedissonUtil redissonUtil;
    
    /**
     * 更新得分
     */
    public void updateScore(String userId, double score) {
        String key = "leaderboard:scores";
        // 添加或更新分数
        redissonUtil.addZset(key, userId, score);
    }
    
    /**
     * 增加得分
     */
    public Double incrementScore(String userId, double increment) {
        String key = "leaderboard:scores";
        // 增加分数
        return redissonUtil.addScoreZset(key, userId, increment);
    }
    
    /**
     * 获取排行榜（降序）
     */
    public List<String> getTopUsers(int count) {
        String key = "leaderboard:scores";
        // 获取排序后的数据
        return redissonUtil.readAllDescZset(key).stream()
                .limit(count)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取排行榜（升序）
     */
    public List<String> getBottomUsers(int count) {
        String key = "leaderboard:scores";
        // 获取排序后的数据
        return redissonUtil.readAllAscZset(key).stream()
                .limit(count)
                .collect(Collectors.toList());
    }
}
```

### 3. 使用List实现队列

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 消息队列服务
 */
@Service
@RequiredArgsConstructor
public class MessageQueueService {

    private final RedissonUtil redissonUtil;
    
    /**
     * 发送消息到队列
     */
    public void sendMessage(String queueName, String message) {
        // 添加到列表末尾
        redissonUtil.addList(queueName, message, Duration.ofDays(1));
    }
    
    /**
     * 批量发送消息
     */
    public void sendMessages(String queueName, List<String> messages) {
        // 批量添加到列表
        redissonUtil.addAllList(queueName, messages);
    }
    
    /**
     * 消费消息
     */
    public String consumeMessage(String queueName) {
        // 从阻塞队列取出消息，超时时间5秒
        try {
            return redissonUtil.pollBlockList(queueName, 5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    /**
     * 批量消费消息
     */
    public List<String> consumeMessages(String queueName, int batchSize) {
        // 批量取出消息
        return redissonUtil.pollBlockList(queueName, batchSize);
    }
}
```

### 4. 使用直接访问特定Redis实例

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description Redis直接访问服务
 */
@Service
@RequiredArgsConstructor
public class DirectRedisAccessService {

    private final RedissonUtil redissonUtil;
    
    /**
     * 仅在特定机房的Redis实例中操作数据
     */
    public void operateInSpecificLocation(String location, String key, String value) {
        RedissonClient client = redissonUtil.getRedissonClient(location);
        if (client != null) {
            client.getBucket(key).set(value);
        } else {
            throw new IllegalArgumentException("未找到指定机房的Redis实例: " + location);
        }
    }
    
    /**
     * 获取主Redis实例
     */
    public RedissonClient getPrimaryClient() {
        return redissonUtil.getRedissonClient();
    }
    
    /**
     * 获取备用Redis实例
     */
    public RedissonClient getBackupClient() {
        return redissonUtil.getBackRedissonClient();
    }
    
    /**
     * 在所有Redis实例上执行相同操作
     */
    public void executeOnAllInstances(String key, String value) {
        // 获取所有可用的Redis实例
        List<RedissonClient> clients = new ArrayList<>();
        clients.add(redissonUtil.getRedissonClient());
        
        RedissonClient backupClient = redissonUtil.getBackRedissonClient();
        if (backupClient != null) {
            clients.add(backupClient);
        }
        
        // 在所有实例上执行操作
        for (RedissonClient client : clients) {
            client.getBucket(key).set(value);
        }
    }
}
```

## 配置参数详解

### 公共配置

| 参数名 | 类型 | 默认值 | 说明 |
| ------ | ---- | ------ | ---- |
| spring.data.redis.password | String | null | 公共Redis密码 |
| spring.data.redis.connection-timeout | Integer | 5000 | 连接超时时间(毫秒) |
| spring.data.redis.response-timeout | Integer | 3000 | 响应超时时间(毫秒) |
| spring.data.redis.idle-connection-timeout | Integer | 10000 | 空闲连接超时时间(毫秒) |
| spring.data.redis.master-connection-pool-size | Integer | 100 | 主节点连接池大小 |
| spring.data.redis.slave-connection-pool-size | Integer | 128 | 从节点连接池大小 |
| spring.data.redis.retry-attempts | Integer | 3 | 重试次数 |
| spring.data.redis.retry-interval | Integer | 1000 | 重试间隔(毫秒) |
| spring.data.redis.check-lock-synced-slaves | Boolean | false | 是否检查锁是否同步到从节点 |
| spring.data.redis.slaves-sync-timeout | Long | 1000 | 从节点同步超时时间(毫秒) |

### 集群配置

| 参数名 | 类型 | 默认值 | 说明 |
| ------ | ---- | ------ | ---- |
| spring.data.redis.cluster.active | Boolean | false | 是否启用集群 |
| spring.data.redis.cluster.location | String | null | 机房位置标识 |
| spring.data.redis.cluster.password | String | null | 集群密码(覆盖公共密码) |
| spring.data.redis.cluster.nodes | List | null | 集群节点列表 |
| spring.data.redis.cluster.netty-threads | Integer | 32 | Netty线程数 |
| spring.data.redis.cluster.max-redirects | Integer | 3 | 最大重定向次数 |

### 备用集群配置

备用集群配置与主集群配置相同，只需将 `cluster` 替换为 `cluster2` 或 `cluster3`。

## 最佳实践

### 1. 合理设置机房位置标识

机房位置标识是区分不同Redis集群的重要依据，建议使用有意义的标识：

```yaml
spring:
  data:
    redis:
      cluster:
        location: SHANGHAI  # 上海机房
      cluster2:
        location: BEIJING   # 北京机房
```

### 2. 异步写入优化

本组件默认会将写操作异步同步到备用Redis集群，但在某些情况下可能需要优化：

- **高频写入**：对于高频写入的场景，可以考虑批量操作减少网络请求
- **延迟敏感**：如果对写入延迟不敏感，可以放心使用异步写入
- **数据一致性**：如果要求强一致性，建议手动管理多Redis实例的写入

### 3. 机房Redis选择策略

在读取数据时，可以根据不同的策略选择从哪个机房读取：

- **就近原则**：根据用户所在地选择最近的机房Redis
- **负载均衡**：在多个Redis实例间进行负载均衡
- **主备模式**：始终从主Redis读取，备用Redis只作为灾备

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description Redis选择策略
 */
@Component
public class RedisLocationStrategy {

    private final RedissonUtil redissonUtil;
    
    @Autowired
    public RedisLocationStrategy(RedissonUtil redissonUtil) {
        this.redissonUtil = redissonUtil;
    }
    
    /**
     * 根据用户IP选择最近的Redis
     */
    public RedissonClient selectByUserIp(String userIp) {
        // 判断用户IP所在区域
        String location = determineLocationByIp(userIp);
        return redissonUtil.getRedissonClient(location);
    }
    
    // 省略IP定位实现
}
```

### 4. 处理Redis不可用情况

配置合理的容错机制，确保一个Redis集群不可用时，系统仍然能够正常工作：

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description Redis容错服务
 */
@Service
@Slf4j
public class RedisFaultTolerantService {

    private final RedissonUtil redissonUtil;
    
    @Autowired
    public RedisFaultTolerantService(RedissonUtil redissonUtil) {
        this.redissonUtil = redissonUtil;
    }
    
    /**
     * 容错获取数据
     */
    public <T> T getWithFallback(String key) {
        try {
            // 先尝试从主Redis获取
            T value = redissonUtil.get(key);
            if (value != null) {
                return value;
            }
            
            // 主Redis没有数据，尝试从备用Redis获取
            RedissonClient backupClient = redissonUtil.getBackRedissonClient();
            if (backupClient != null) {
                return (T) backupClient.getBucket(key).get();
            }
            
            return null;
        } catch (Exception e) {
            log.error("Redis读取异常，尝试从备用实例获取", e);
            // 主Redis异常，尝试从备用Redis获取
            try {
                RedissonClient backupClient = redissonUtil.getBackRedissonClient();
                if (backupClient != null) {
                    return (T) backupClient.getBucket(key).get();
                }
            } catch (Exception ex) {
                log.error("所有Redis实例都不可用", ex);
            }
            return null;
        }
    }
}
```

## 应用场景

- **多区域部署**：应用部署在多个地区，需要将数据同步到各个区域的Redis
- **数据容灾备份**：将数据同时写入多个Redis集群，实现灾备
- **就近访问加速**：用户访问就近的Redis集群，降低延迟
- **读写分离**：主Redis实例负责写操作，从Redis实例负责读操作
- **灰度发布**：新功能先在一个Redis集群上测试，确认无误后再推广
- **流量分担**：将不同类型的数据存储在不同的Redis集群，分担负载

## 常见问题

### 1. 多Redis实例同步失败怎么办？

当异步同步到备用Redis失败时，系统会记录错误日志但不会影响主流程。如果需要更可靠的同步，建议：

1. 实现自定义的重试机制
2. 使用消息队列进行数据同步
3. 定期进行数据校验和修复

### 2. 如何监控多个Redis实例？

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

    private final RedissonUtil redissonUtil;
    
    @Autowired
    public RedisMonitorService(RedissonUtil redissonUtil) {
        this.redissonUtil = redissonUtil;
    }
    
    /**
     * 检查所有Redis实例可用性
     */
    @Scheduled(fixedRate = 60000)  // 每分钟执行一次
    public void checkRedisAvailability() {
        Map<String, Boolean> status = new HashMap<>();
        
        // 检查主Redis
        try {
            RedissonClient client = redissonUtil.getRedissonClient();
            client.getBucket("health:check").set("ok");
            status.put("primary", true);
        } catch (Exception e) {
            log.error("主Redis不可用", e);
            status.put("primary", false);
        }
        
        // 检查备用Redis
        try {
            RedissonClient backupClient = redissonUtil.getBackRedissonClient();
            if (backupClient != null) {
                backupClient.getBucket("health:check").set("ok");
                status.put("backup", true);
            }
        } catch (Exception e) {
            log.error("备用Redis不可用", e);
            status.put("backup", false);
        }
        
        log.info("Redis实例状态: {}", status);
    }
}
```

### 3. 如何处理Redis配置变更？

Redis配置变更通常需要重启应用，但可以实现动态刷新机制：

1. 使用Spring Cloud Config或其他配置中心动态更新配置
2. 实现自定义的Redis连接池管理器，支持动态刷新
3. 使用监听器监听配置变更，重新初始化Redis连接

## 贡献 🙏

欢迎提交Issue或Pull Request参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。

## 致谢

本项目基于 [Redisson](https://github.com/redisson/redisson) 开发，感谢 Redisson 项目团队提供的优秀分布式工具。 