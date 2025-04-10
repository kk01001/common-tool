# 分布式锁 Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/lock-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.kk01001/lock-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 基于 [Redisson](https://github.com/redisson/redisson) 的轻量级分布式锁解决方案，提供便捷的注解式锁定义，支持多种锁类型和灵活的锁策略配置。

## 背景

在微服务和分布式系统中，多个服务实例可能同时访问和修改共享资源，如果不加以控制，容易导致数据竞争、数据不一致等问题。分布式锁作为一种并发控制机制，能有效解决这类问题。

传统的基于数据库或单机锁的方案往往存在性能瓶颈或无法应对分布式场景。`lock-spring-boot-starter` 基于 Redisson 提供了强大的分布式锁实现，同时封装了易用的注解接口，大大简化了分布式锁的使用难度。

## 项目目标

- **简单易用**：通过注解方式定义锁，简化开发流程
- **功能丰富**：支持多种锁类型，满足不同业务场景需求
- **高可靠**：基于 Redisson 提供的可靠分布式锁实现
- **易扩展**：支持自定义锁策略，灵活适应各种业务需求
- **低侵入**：对业务代码几乎零侵入，提高代码可维护性

## 核心功能与亮点 ✨

- **多锁类型支持**：提供可重入锁、公平锁、自旋锁、信号量等多种锁类型
- **注解驱动**：通过 `@Lock` 注解实现简洁的锁控制
- **SpEL表达式支持**：使用表达式动态构造锁键，提高灵活性
- **优雅降级**：支持锁开关控制，可在不改代码的情况下禁用锁
- **自动配置**：与 Spring Boot 无缝集成，零配置即可使用
- **阻塞/非阻塞模式**：支持阻塞和非阻塞两种锁获取方式

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- Redisson 3.x
- Spring AOP

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>lock-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 配置Redis连接

在 `application.yml` 或 `application.properties` 中添加 Redisson 的配置：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_password   # 如果有密码
    database: 0
```

### 使用分布式锁

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 订单服务
 */
@Service
@Slf4j
public class OrderService {

    /**
     * 使用默认的可重入锁
     */
    @Lock(key = "'order:create:' + #orderId")
    public void createOrder(String orderId, OrderDTO orderDTO) {
        // 业务逻辑
        log.info("创建订单: {}", orderId);
        // 模拟业务处理耗时
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 使用Redisson锁，设置超时时间
     */
    @Lock(
        type = LockType.REDISSON_LOCK,
        key = "'order:pay:' + #orderId",
        timeout = 5000,
        timeunit = TimeUnit.MILLISECONDS
    )
    public void payOrder(String orderId, BigDecimal amount) {
        // 支付处理逻辑
        log.info("订单支付处理: {}, 金额: {}", orderId, amount);
    }
    
    /**
     * 使用Redisson公平锁
     */
    @Lock(
        type = LockType.REDISSON_FAIR_LOCK,
        key = "'inventory:deduct:' + #productId",
        timeout = 3000
    )
    public void deductInventory(Long productId, Integer quantity) {
        // 库存扣减逻辑
        log.info("扣减商品库存: {}, 数量: {}", productId, quantity);
    }
    
    /**
     * 使用信号量控制并发数
     */
    @Lock(
        type = LockType.REDISSON_SEMAPHORE,
        key = "'order:concurrent'",
        permits = 10
    )
    public void processHighConcurrentOrder(OrderDTO orderDTO) {
        // 高并发订单处理逻辑
        log.info("处理高并发订单: {}", orderDTO.getOrderId());
    }
}
```

## 锁类型详解

| 锁类型 | 说明 | 适用场景 |
| ----- | ---- | ------- |
| REENTRANT_LOCK | 本地可重入锁 | 单机应用，同一JVM内的线程同步 |
| SEMAPHORE | 本地信号量 | 单机应用，限制并发访问数量 |
| REDISSON_LOCK | Redisson可重入锁 | 分布式环境，需要互斥访问共享资源 |
| REDISSON_FAIR_LOCK | Redisson公平锁 | 分布式环境，需要按请求顺序获取锁 |
| REDISSON_SPIN_LOCK | Redisson自旋锁 | 分布式环境，期望快速获取锁，等待时间短 |
| REDISSON_READ_WRITE_LOCK | Redisson读写锁 | 分布式环境，读多写少的场景 |
| REDISSON_SEMAPHORE | Redisson信号量 | 分布式环境，限制并发访问数量 |
| REDIS_TEMPLATE_SEMAPHORE | RedisTemplate信号量 | 使用RedisTemplate实现的信号量 |

## @Lock注解参数详解

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | ----- | ---- |
| type | LockType | LockType.REENTRANT_LOCK | 锁类型 |
| redisClientType | RedisClientType | RedisClientType.REDISSON | Redis客户端类型 |
| enable | boolean | true | 是否启用锁 |
| block | boolean | true | 是否为阻塞锁 |
| key | String | "defaultLockKey" | 锁的键，支持SpEL表达式 |
| permits | int | 10 | 信号量的许可数（仅信号量类型有效） |
| fair | boolean | true | 是否为公平锁（仅部分类型有效） |
| timeout | long | 1000L | 获取锁的超时时间 |
| timeunit | TimeUnit | TimeUnit.MILLISECONDS | 时间单位 |
| permitsFunction | String | "" | 动态计算许可数的SpEL表达式方法 |
| ruleFunction | String | "" | 自定义规则查询方法，优先级最高 |

## 高级用法

### 1. 使用SpEL表达式构建动态锁键

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 使用复杂SpEL表达式构建锁键
 */
@Service
public class AdvancedOrderService {

    /**
     * 使用SpEL表达式构建复杂锁键
     * 组合多个参数
     */
    @Lock(key = "'order:' + #order.userId + ':' + #order.productId")
    public void placeOrder(OrderRequest order) {
        // 下单逻辑
    }
    
    /**
     * 使用对象方法
     */
    @Lock(key = "#order.generateLockKey()")
    public void updateOrder(Order order) {
        // 更新订单逻辑
    }
    
    /**
     * 使用Bean方法
     */
    @Lock(key = "@lockKeyGenerator.generateKey(#userId, #action)")
    public void performUserAction(Long userId, String action) {
        // 用户操作逻辑
    }
}
```

### 2. 非阻塞锁使用

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 非阻塞锁示例
 */
@Service
@Slf4j
public class NonBlockingService {

    /**
     * 使用非阻塞锁
     * 当无法获取锁时会抛出异常
     */
    @Lock(
        key = "'resource:' + #resourceId",
        block = false,
        timeout = 100
    )
    public void accessResource(String resourceId) {
        log.info("访问资源: {}", resourceId);
    }
    
    /**
     * 优雅处理非阻塞锁失败
     */
    public void tryAccessResource(String resourceId) {
        try {
            accessResource(resourceId);
            // 成功获取锁后的处理
        } catch (LockException e) {
            // 获取锁失败的处理
            log.warn("资源正忙，请稍后再试: {}", resourceId);
        }
    }
}
```

### 3. 自定义锁规则

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 锁规则生成器
 */
@Component
public class LockRuleGenerator {

    /**
     * 根据业务类型生成不同的锁规则
     */
    public LockRule generateRule(String businessType, String resourceId) {
        if ("highPriority".equals(businessType)) {
            // 高优先级业务使用更长的超时时间
            return LockRule.builder()
                    .lockType(LockType.REDISSON_FAIR_LOCK)
                    .key("business:" + businessType + ":" + resourceId)
                    .timeout(10000L)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .build();
        } else {
            // 普通业务使用标准配置
            return LockRule.builder()
                    .lockType(LockType.REDISSON_LOCK)
                    .key("business:" + businessType + ":" + resourceId)
                    .timeout(3000L)
                    .timeUnit(TimeUnit.MILLISECONDS)
                    .build();
        }
    }
}

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 使用自定义规则的服务
 */
@Service
public class CustomRuleService {

    /**
     * 使用自定义规则函数
     */
    @Lock(ruleFunction = "@lockRuleGenerator.generateRule(#businessType, #resourceId)")
    public void processBusinessResource(String businessType, String resourceId) {
        // 业务处理逻辑
    }
}
```

### 4. 使用信号量控制并发数

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 限流服务
 */
@Service
public class RateLimitService {

    /**
     * 使用信号量限制API并发访问量
     */
    @Lock(
        type = LockType.REDISSON_SEMAPHORE,
        key = "'api:rateLimit:' + #apiName",
        permits = 100  // 最多100个并发请求
    )
    public void processApiRequest(String apiName, RequestData data) {
        // API请求处理逻辑
    }
    
    /**
     * 根据用户等级动态设置并发限制
     */
    @Lock(
        type = LockType.REDISSON_SEMAPHORE,
        key = "'user:limit:' + #userType",
        permitsFunction = "@userLevelService.getMaxConcurrent(#userType)"
    )
    public void processUserRequest(String userType, RequestData data) {
        // 用户请求处理逻辑
    }
}
```

## 最佳实践

### 1. 锁粒度选择

- **粗粒度锁**：锁定整个资源集合，实现简单但并发度低
  ```java
  @Lock(key = "'inventory'")
  ```

- **细粒度锁**：只锁定特定资源，提高并发度
  ```java
  @Lock(key = "'inventory:' + #productId")
  ```

### 2. 锁超时设置

合理设置锁的超时时间，避免以下问题：
- **超时过短**：业务未处理完锁就释放，导致并发问题
- **超时过长**：某个操作异常时，长时间阻塞其他线程

推荐根据业务复杂度和执行时间来设置：
```java
@Lock(
    key = "'order:process:' + #orderId",
    timeout = 5000,  // 预估业务执行时间的2-3倍
    timeunit = TimeUnit.MILLISECONDS
)
```

### 3. 避免锁嵌套

尽量避免嵌套锁，可能导致死锁或性能问题：
```java
// 避免这样的嵌套锁调用
@Lock(key = "'outer:' + #id")
public void outerMethod(String id) {
    // ...
    innerMethod(id);  // 内部再次获取锁
    // ...
}

@Lock(key = "'inner:' + #id")
public void innerMethod(String id) {
    // ...
}
```

如果必须嵌套，请确保：
1. 使用可重入锁类型
2. 锁的获取顺序一致，避免死锁
3. 合理设置超时时间

### 4. 异常处理

确保在使用锁的方法中妥善处理异常，避免锁无法释放：
```java
@Lock(key = "'resource:' + #id")
public void processResource(String id) {
    try {
        // 业务逻辑
    } catch (Exception e) {
        // 异常处理
        log.error("处理资源时发生错误", e);
        // 可能需要补偿操作
        throw e;  // 重新抛出异常，让AOP能够释放锁
    }
}
```

## 应用场景

- **秒杀系统**：限制并发访问，防止超卖
- **库存管理**：确保库存扣减的原子性
- **订单处理**：防止重复下单和支付
- **账户操作**：确保账户金额变更的安全性
- **任务调度**：防止分布式环境下的任务重复执行
- **限流控制**：限制接口的并发访问量
- **防重复提交**：防止表单或请求的重复提交

## 常见问题

### 1. 锁释放失败怎么办？

锁释放失败的主要原因和解决方法：

- **执行超时**：设置合理的锁超时时间，避免锁长时间不释放
- **程序异常**：确保异常被正确捕获并向上传播，使AOP能够释放锁
- **Redis连接问题**：配置Redis连接池参数，确保连接稳定性

### 2. 如何选择合适的锁类型？

- **一般场景**：使用 `REDISSON_LOCK`
- **要求请求排队有序**：使用 `REDISSON_FAIR_LOCK`
- **读多写少的场景**：使用 `REDISSON_READ_WRITE_LOCK`
- **限制并发数量**：使用 `REDISSON_SEMAPHORE`
- **需要快速尝试获取锁**：使用 `REDISSON_SPIN_LOCK`

### 3. 分布式锁性能问题

提高分布式锁性能的方法：

1. **合理设置锁粒度**：细粒度锁提高并发度
2. **使用本地缓存**：减少频繁锁操作
3. **优化Redis配置**：确保Redis高可用和低延迟
4. **批量处理**：减少锁操作频率

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 高性能处理示例
 */
@Service
public class HighPerformanceService {

    /**
     * 批量处理减少锁操作次数
     */
    @Lock(key = "'batch:process'")
    public void batchProcess(List<String> items) {
        // 一次性处理多个项目
        items.forEach(this::processItem);
    }
    
    private void processItem(String item) {
        // 处理单个项目
        // 注意这里不需要再加锁
    }
}
```

## 贡献 🙏

欢迎提交Issue或Pull Request参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。

## 致谢

本项目基于 [Redisson](https://github.com/redisson/redisson) 开发，感谢 Redisson 项目团队提供的优秀分布式解决方案。
