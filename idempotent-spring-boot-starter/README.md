# 接口幂等性 Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/idempotent-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.archer099/idempotent-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 基于Redis的轻量级接口幂等性校验解决方案，通过注解轻松实现API接口的幂等性保证，防止重复请求引起的数据异常。

## 背景

在分布式系统和微服务架构中，由于网络抖动、前端重复提交等原因，同一个请求可能会多次到达服务端。如果这些请求涉及数据修改操作（如订单创建、支付等），就可能导致数据重复或不一致。

幂等性是指对同一个操作执行任意多次，产生的结果与执行一次相同。`idempotent-spring-boot-starter` 提供了一种简单的方式来保证接口幂等性，防止重复请求带来的数据问题。

## 项目目标

- **简单易用**：通过注解即可实现接口幂等性控制
- **灵活配置**：支持全局配置和方法级别的精细化配置
- **高性能**：基于Redis的分布式锁机制，保证高并发场景下的可靠性
- **可扩展**：支持自定义幂等键生成策略
- **无侵入**：对业务代码无侵入，只需添加注解即可

## 核心功能与亮点 ✨

- **注解驱动**：使用`@Idempotent`注解即可轻松实现接口幂等性
- **SpEL表达式支持**：灵活定义幂等键生成规则
- **自动集成**：与Spring Boot自动配置无缝集成
- **异常处理**：提供友好的异常信息，便于问题定位
- **生命周期管理**：自动管理幂等键的过期时间，避免资源浪费

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- Redis (基于Redisson)
- Spring AOP

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>idempotent-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 配置属性

在`application.yml`或`application.properties`中添加以下配置：

```yaml
idempotent:
  # 启用幂等性校验（默认为true）
  enabled: true
  # 幂等键前缀（默认为idempotent:）
  key-prefix: idempotent:
  # 默认过期时间，单位秒（默认为10秒）
  default-expire-seconds: 10
  # 是否打印调试日志（默认为false）
  debug-log: false
```

### 在接口上使用

在需要保证幂等性的接口方法上添加`@Idempotent`注解：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 订单控制器
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    /**
     * 创建订单接口
     * 使用默认配置，基于整个请求体生成幂等键
     */
    @PostMapping("/create")
    @Idempotent
    public Result createOrder(@RequestBody CreateOrderRequest request) {
        // 创建订单业务逻辑
        return Result.success();
    }
    
    /**
     * 支付订单接口
     * 使用自定义幂等键和过期时间
     */
    @PostMapping("/pay")
    @Idempotent(key = "#request.orderId", expire = 60)
    public Result payOrder(@RequestBody PayOrderRequest request) {
        // 支付订单业务逻辑
        return Result.success();
    }
    
    /**
     * 取消订单接口
     * 指定自定义前缀
     */
    @PostMapping("/cancel")
    @Idempotent(keyPrefix = "order:cancel:", key = "#request.orderId")
    public Result cancelOrder(@RequestBody CancelOrderRequest request) {
        // 取消订单业务逻辑
        return Result.success();
    }
}
```

## 注解参数详解

`@Idempotent`注解提供了以下可配置参数：

| 参数 | 说明 | 默认值 | 示例 |
| --- | --- | --- | --- |
| keyPrefix | 幂等键前缀 | idempotent: | @Idempotent(keyPrefix = "order:") |
| key | 幂等键表达式（SpEL） | 空（默认使用请求体的MD5值） | @Idempotent(key = "#request.orderId") |
| expire | 过期时间（秒） | 10 | @Idempotent(expire = 60) |

## 自定义幂等键生成

### 基于SpEL表达式

`@Idempotent`注解的`key`属性支持SpEL表达式，可以灵活定义幂等键的生成规则：

```java
// 使用单个请求参数
@Idempotent(key = "#orderId")
public Result method(@RequestParam String orderId) { ... }

// 使用请求对象的属性
@Idempotent(key = "#request.userId + '_' + #request.orderId")
public Result method(@RequestBody OrderRequest request) { ... }

// 使用多个参数组合
@Idempotent(key = "#userId + '_' + #productId")
public Result method(@RequestParam String userId, @RequestParam String productId) { ... }

// 调用静态方法
@Idempotent(key = "T(java.util.UUID).randomUUID().toString()")
public Result method() { ... }

// 使用Spring Bean方法
@Idempotent(key = "@myService.generateKey(#request)")
public Result method(@RequestBody Request request) { ... }
```

### 完全自定义键生成器

如果内置的键生成策略无法满足需求，可以扩展`IdempotentKeyGenerator`类：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 自定义幂等键生成器
 */
@Component
public class CustomIdempotentKeyGenerator extends IdempotentKeyGenerator {
    
    @Override
    public String generateKey(String keyExpression, String keyPrefix, ProceedingJoinPoint joinPoint, 
                             BeanFactoryResolver beanFactoryResolver) {
        // 你可以完全覆盖默认实现，实现自己的键生成逻辑
        if (SpecialCase.matches(joinPoint)) {
            return keyPrefix + "special:" + SpecialCase.getKey(joinPoint);
        }
        
        // 或者调用父类方法，保留默认逻辑
        return super.generateKey(keyExpression, keyPrefix, joinPoint, beanFactoryResolver);
    }
}
```

## 幂等性异常处理

当检测到重复请求时，框架会抛出`IdempotentException`异常。你可以通过全局异常处理器捕获并处理这个异常：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IdempotentException.class)
    public Result handleIdempotentException(IdempotentException e) {
        log.warn("检测到重复请求: {}", e.getMessage());
        return Result.error(1001, "请勿重复提交");
    }
}
```

## 高级用法

### 1. 条件性幂等检查

在某些场景下，可能需要根据条件来决定是否执行幂等性检查。可以结合Spring AOP的其他功能实现：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 条件性幂等切面
 */
@Aspect
@Component
@RequiredArgsConstructor
public class ConditionalIdempotentAspect {

    private final RedisIdempotentExecutor executor;
    private final IdempotentKeyGenerator keyGenerator;
    
    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) && args(request,..)")
    public Object checkIdempotent(ProceedingJoinPoint joinPoint, Object request) throws Throwable {
        if (shouldCheckIdempotent(request)) {
            // 执行幂等性检查
            String key = "conditional:" + request.hashCode();
            executor.execute(key, 10);
        }
        return joinPoint.proceed();
    }
    
    private boolean shouldCheckIdempotent(Object request) {
        // 实现你的条件逻辑
        return request instanceof SensitiveOperation;
    }
}
```

### 2. 自定义幂等性存储

默认情况下，框架使用Redis作为幂等键的存储。如果你需要使用其他存储方式，可以实现自定义的幂等执行器：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 自定义幂等执行器
 */
@Component
public class CustomIdempotentExecutor {
    
    // 自定义存储实现
    private final IdempotentStorage storage;
    
    public void execute(String key, long expireSeconds) {
        boolean success = storage.setIfAbsent(key, expireSeconds);
        if (!success) {
            throw new IdempotentException("重复请求");
        }
    }
}
```

### 3. 异步请求幂等处理

对于异步处理的请求，可能需要在处理完成后再释放幂等锁：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 异步请求处理器
 */
@Service
@RequiredArgsConstructor
public class AsyncRequestProcessor {

    private final RedissonClient redissonClient;
    
    @Async
    public CompletableFuture<Result> processAsync(ProcessRequest request) {
        String idempotentKey = "async:" + request.getRequestId();
        RLock lock = redissonClient.getLock(idempotentKey);
        
        try {
            // 获取锁
            if (!lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                return CompletableFuture.completedFuture(Result.error("请求处理中"));
            }
            
            // 执行异步处理逻辑
            return doProcess(request);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(Result.error("处理失败"));
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    private CompletableFuture<Result> doProcess(ProcessRequest request) {
        // 实际的异步处理逻辑
        // ...
    }
}
```

## 最佳实践

1. **适当的幂等键设计**
   - 针对业务特点选择合适的幂等键，例如对于订单创建可以使用订单号、用户ID等唯一标识
   - 避免使用过于通用的键，以免误判不同的业务请求

2. **合理的过期时间**
   - 根据业务特性设置适当的过期时间，过短可能导致重复请求通过，过长则占用更多资源
   - 对于长时间运行的任务，考虑使用更长的过期时间

3. **异常处理**
   - 为幂等性异常提供友好的错误信息，便于前端处理和用户理解
   - 考虑在某些场景下返回上一次操作的结果，而不是简单的错误信息

4. **与分布式事务结合**
   - 在复杂业务场景中，考虑将幂等性控制与分布式事务结合使用
   - 确保在分布式系统中的一致性

## 注意事项

1. **GET请求默认不检查**
   - 框架默认只对POST和PUT请求进行幂等性检查，如需对GET请求也进行检查，需自行扩展

2. **Redis可用性**
   - 幂等性控制依赖Redis，确保Redis的高可用性

3. **SpEL表达式安全性**
   - 使用SpEL表达式时，注意避免注入安全风险

4. **与重试机制的协调**
   - 如果系统中有自动重试机制，需要协调幂等性控制与重试策略

## 应用场景

- **支付系统**：防止重复支付
- **订单系统**：防止重复下单
- **用户注册**：防止重复注册
- **库存系统**：防止重复扣减
- **消息投递**：确保消息只被处理一次

## 贡献 🙏

欢迎提交Issue或Pull Request参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。 