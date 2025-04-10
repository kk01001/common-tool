# Rate Limiter Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/rate-limiter-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.kk01001/rate-limiter-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 集成多种限流算法和实现策略的 Spring Boot Starter，支持本地限流和分布式限流，提供注解驱动的简易使用方式，帮助系统轻松应对高并发场景。

## 背景

在微服务和分布式系统中，限流是保护系统稳定性的重要手段。当面对突发流量或恶意请求时，如果没有有效的限流措施，很容易导致系统过载、响应变慢甚至宕机。虽然市面上已有不少限流工具，但往往需要较多的配置工作，且不同场景下的实现方式各异。

`rate-limiter-spring-boot-starter` 旨在提供一套统一、易用的限流解决方案，集成了主流的限流算法和实现策略，支持本地内存和分布式Redis两种模式，开发者只需通过简单的注解即可实现对接口或方法的限流保护。

## 项目目标

- **多算法支持**：集成固定窗口、滑动窗口、令牌桶、漏桶等多种限流算法
- **多策略实现**：支持本地内存、Guava、Redisson、Redis Lua脚本等多种实现方式
- **易于使用**：通过简单的注解即可启用限流功能
- **灵活配置**：支持固定配置和动态配置两种方式
- **分布式支持**：内置Redis分布式限流实现，满足集群环境需求

## 核心功能与亮点 ✨

- **注解驱动**：通过 `@RateLimiter` 注解即可启用限流
- **多种限流模式**：
  - 本地限流：基于JVM内存，适用于单机场景
  - Guava限流：基于Google Guava，提供令牌桶算法实现
  - Redis限流：基于Redis的分布式限流，支持集群环境
  - Redisson限流：基于Redisson的RRateLimiter实现
- **丰富的算法**：
  - 固定窗口算法：最简单的限流算法，适合简单场景
  - 滑动窗口算法：解决固定窗口边界问题，更加平滑
  - 令牌桶算法：允许一定程度的突发流量，同时保证平均速率
  - 漏桶算法：严格控制流出速率，适合需要稳定处理速率的场景
- **灵活的规则配置**：
  - 支持注解直接配置
  - 支持SpEL表达式动态配置
  - 支持自定义规则方法获取配置
- **异常处理**：统一的限流异常处理机制

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- Spring AOP
- Redisson
- Redis
- Google Guava
- Lombok
- Hutool

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>rate-limiter-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 基本使用

使用 `@RateLimiter` 注解标记需要限流的方法：

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 商品服务
 */
@Service
public class ProductService {

    /**
     * 默认使用本地限流，每秒最多处理10个请求
     */
    @RateLimiter
    public ProductVO getProductById(String productId) {
        // 业务逻辑
        return productService.findById(productId);
    }

    /**
     * 使用Redis滑动窗口限流，自定义key，每分钟最多处理100个请求
     */
    @RateLimiter(
        type = RateLimiterType.REDIS_LUA_SLIDING_WINDOW,
        key = "'product:detail:' + #productId",
        maxRequests = 100,
        windowTime = 60
    )
    public ProductDetailVO getProductDetail(String productId) {
        // 业务逻辑
        return productDetailService.findByProductId(productId);
    }

    /**
     * 使用令牌桶算法限流，适合允许一定突发流量的场景
     */
    @RateLimiter(
        type = RateLimiterType.REDIS_LUA_TOKEN_BUCKET,
        key = "'product:create'",
        bucketCapacity = 100,   // 桶容量为100
        tokenRate = 10          // 每秒生成10个令牌
    )
    public void createProduct(ProductCreateDTO product) {
        // 业务逻辑
        productRepository.save(product);
    }
}
```

### 异常处理

可以通过 `@ExceptionHandler` 捕获 `RateLimitException` 进行统一处理：

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse> handleRateLimitException(RateLimitException ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error("请求过于频繁，请稍后再试"));
    }
}
```

## 限流算法详解

### 1. 固定窗口算法

将时间分割为固定大小的窗口，在每个窗口内限制最大请求数。

```java
@RateLimiter(
    type = RateLimiterType.REDIS_LUA_FIXED_WINDOW,
    key = "'api:fixed:' + #userId",
    maxRequests = 100,
    windowTime = 60
)
```

**适用场景**：简单的API限流，对精确性要求不高的场景

**优点**：实现简单，容易理解
**缺点**：存在窗口边界问题，可能出现短时间内的流量突增

### 2. 滑动窗口算法

将固定窗口细分为多个小窗口，随着时间推移，窗口也会滑动，旧的小窗口过期，新的小窗口加入。

```java
@RateLimiter(
    type = RateLimiterType.REDIS_LUA_SLIDING_WINDOW,
    key = "'api:sliding:' + #userId",
    maxRequests = 100,
    windowTime = 60
)
```

**适用场景**：需要较为平滑限流效果的API接口

**优点**：解决了固定窗口的边界突增问题，流量更加平滑
**缺点**：实现稍复杂，占用较多存储空间

### 3. 令牌桶算法

系统以固定速率向桶中放入令牌，请求需要获取令牌才能被处理，令牌不足时请求会被拒绝。

```java
@RateLimiter(
    type = RateLimiterType.REDIS_LUA_TOKEN_BUCKET,
    key = "'api:token:' + #userId",
    bucketCapacity = 100,   // 桶容量
    tokenRate = 10          // 每秒生成令牌数
)
```

**适用场景**：需要允许一定突发流量的场景，如API网关

**优点**：支持突发流量，平均速率稳定
**缺点**：初始状态可能允许大量请求通过

### 4. 漏桶算法

请求进入桶中，然后以固定的速率流出并被处理，桶满时请求被拒绝。

```java
@RateLimiter(
    type = RateLimiterType.REDIS_LUA_LEAKY_BUCKET,
    key = "'api:leaky:' + #userId",
    bucketCapacity = 100,   // 桶容量
    tokenRate = 10,         // 漏出速率
    permits = 1             // 本次申请凭证数
)
```

**适用场景**：需要稳定处理速率的场景，如数据库写入

**优点**：严格控制处理速率，保护后端系统
**缺点**：不支持突发流量，请求可能被延迟处理

## 限流策略实现

### 1. 本地限流 (LOCAL)

基于JVM内存的限流实现，只适用于单机场景。

**优点**：实现简单，速度快
**缺点**：不支持分布式场景，服务重启后状态丢失

### 2. Guava限流 (GUAVA)

基于Google Guava的RateLimiter实现，支持令牌桶算法。

**优点**：实现成熟，性能好
**缺点**：不支持分布式场景

### 3. Redisson限流 (REDISSON)

基于Redisson的RRateLimiter实现，支持分布式场景。

**优点**：支持分布式，使用简单
**缺点**：功能相对有限，只支持固定速率

### 4. Redis Lua脚本限流

基于Redis Lua脚本实现的限流，支持多种限流算法。

**优点**：支持分布式，实现灵活，支持多种算法
**缺点**：依赖Redis，实现复杂

## 高级配置

### 动态限流参数

通过SpEL表达式动态设置限流参数：

```java
@RateLimiter(
    type = RateLimiterType.REDIS_LUA_SLIDING_WINDOW,
    key = "'user:' + #user.id",
    maxRequestsFunction = "#user.vipLevel * 10 + 50",  // VIP用户获得更高限额
    windowTimeFunction = "60"
)
public UserInfoVO getUserInfo(User user) {
    // 业务逻辑
}
```

### 自定义规则获取

可以通过自定义方法获取限流规则：

```java
@RateLimiter(
    ruleFunction = "getRateLimitRule(#userId)"
)
public OrderVO getOrderList(String userId) {
    // 业务逻辑
}

// 在当前类中定义规则获取方法
private FlowRule getRateLimitRule(String userId) {
    // 从配置中心、数据库或其他地方获取规则
    return FlowRule.builder()
            .enable(true)
            .rateLimiterType(RateLimiterType.REDIS_LUA_SLIDING_WINDOW)
            .key("order:list:" + userId)
            .maxRequests(userService.isVip(userId) ? 100 : 50)
            .windowTime(60)
            .build();
}
```

## 最佳实践

1. **选择合适的限流算法**：
   - 简单场景使用固定窗口
   - 需要平滑效果使用滑动窗口
   - 允许突发流量使用令牌桶
   - 需要稳定处理速率使用漏桶

2. **设置合理的限流阈值**：
   - 基于系统容量和业务需求设置
   - 重要接口可以设置较高阈值
   - 非核心接口可以设置较低阈值

3. **区分用户等级**：
   - 为不同用户等级设置不同的限流策略
   - VIP用户可以设置更高限额

4. **使用有意义的限流键**：
   - 按接口路径限流：`'api:' + #request.requestURI`
   - 按用户ID限流：`'user:' + #userId`
   - 按IP限流：`'ip:' + #request.remoteAddr`

5. **监控限流指标**：
   - 记录被限流的请求数
   - 监控限流规则的触发情况

## 应用场景

- **API接口保护**：防止接口被恶意调用或突发流量冲击
- **敏感操作限制**：限制用户敏感操作频率，如登录、支付等
- **第三方API调用限制**：控制对外部依赖的调用频率
- **后端资源保护**：保护数据库等后端资源不被过载
- **多租户资源隔离**：为不同租户设置不同的限流策略

## 贡献 🙏

欢迎提交 Issue 或 Pull Request 参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。 