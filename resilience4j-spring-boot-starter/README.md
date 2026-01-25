# Resilience4j Spring Boot Starter

基于 Resilience4j 的容错组件，提供熔断器、限流器、重试、舱壁、时间限制器等完整功能。

## 功能特性

### 核心模块

1. **熔断器（Circuit Breaker）** - 服务熔断保护
2. **限流器（Rate Limiter）** - 请求限流控制
3. **重试（Retry）** - 失败重试机制
4. **舱壁（Bulkhead）** - 并发控制和资源隔离
5. **时间限制器（Time Limiter）** - 超时控制

### 特性

- 注解驱动，简单易用
- 支持多种降级策略
- 灵活的配置方式
- 完整的监控指标
- 支持组合使用

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>resilience4j-spring-boot-starter</artifactId>
    <version>2.4.9</version>
</dependency>
```

### 2. 基础使用

```java
@Service
public class UserService {
    
    // 熔断器
    @CircuitBreaker(name = "userService")
    public User getUser(Long id) {
        return remoteService.getUser(id);
    }
    
    // 限流器
    @RateLimiter(name = "userService", limitForPeriod = 10)
    public List<User> listUsers() {
        return remoteService.listUsers();
    }
    
    // 重试
    @Retry(name = "userService", maxAttempts = 3)
    public void updateUser(User user) {
        remoteService.updateUser(user);
    }
}
```

## 模块详解

### 1. 熔断器（Circuit Breaker）

自动检测服务故障并触发熔断，防止故障扩散。

#### 基础使用

```java
@CircuitBreaker(
    name = "orderService",
    failureRateThreshold = 50.0f,
    slowCallRateThreshold = 80.0f,
    slowCallDurationThreshold = 3000
)
public Order createOrder(OrderRequest request) {
    return orderService.create(request);
}
```

#### 配置说明

```yaml
resilience4j:
  circuit-breaker:
    instances:
      orderService:
        failure-rate-threshold: 50.0      # 失败率阈值50%
        slow-call-rate-threshold: 80.0    # 慢调用率阈值80%
        slow-call-duration-threshold: 3000 # 慢调用时间3秒
        sliding-window-size: 100          # 滑动窗口大小
        minimum-number-of-calls: 10       # 最小调用次数
```

#### 降级处理

```java
@CircuitBreaker(
    name = "orderService",
    fallbackStrategy = FallbackStrategy.METHOD,
    fallbackMethod = "createOrderFallback"
)
public Order createOrder(OrderRequest request) {
    return orderService.create(request);
}

public Order createOrderFallback(OrderRequest request, Throwable throwable) {
    log.error("创建订单失败，使用降级逻辑", throwable);
    return Order.failed();
}
```

### 2. 限流器（Rate Limiter）

控制请求速率，防止系统过载。

#### 基础使用

```java
@RateLimiter(
    name = "apiService",
    limitForPeriod = 10,           // 每个周期允许10个请求
    limitRefreshPeriod = 1000000000, // 1秒（纳秒）
    timeoutDuration = 5000          // 等待超时5秒
)
public ApiResponse callApi(ApiRequest request) {
    return apiClient.call(request);
}
```

#### 配置说明

```yaml
resilience4j:
  rate-limiter:
    instances:
      apiService:
        limit-for-period: 10
        limit-refresh-period: 1000000000  # 纳秒
        timeout-duration: 5000            # 毫秒
```

### 3. 重试（Retry）

失败后自动重试，提高成功率。

#### 基础使用

```java
@Retry(
    name = "paymentService",
    maxAttempts = 3,
    waitDuration = 1000,
    retryExceptions = {TimeoutException.class, RemoteException.class}
)
public PaymentResult pay(PaymentRequest request) {
    return paymentGateway.pay(request);
}
```

#### 配置说明

```yaml
resilience4j:
  retry:
    instances:
      paymentService:
        max-attempts: 3
        wait-duration: 1000  # 毫秒
```

#### 指数退避

```java
@Retry(
    name = "paymentService",
    maxAttempts = 5,
    waitDuration = 1000  // 初始等待1秒，后续指数增长
)
public PaymentResult pay(PaymentRequest request) {
    return paymentGateway.pay(request);
}
```

### 4. 舱壁（Bulkhead）

限制并发调用数，实现资源隔离。

#### 信号量模式

```java
@Bulkhead(
    name = "reportService",
    maxConcurrentCalls = 10,
    type = Bulkhead.Type.SEMAPHORE
)
public Report generateReport(Long id) {
    return reportGenerator.generate(id);
}
```

#### 线程池模式

```java
@Bulkhead(
    name = "reportService",
    maxConcurrentCalls = 5,
    type = Bulkhead.Type.THREADPOOL
)
public Report generateReport(Long id) {
    return reportGenerator.generate(id);
}
```

#### 配置说明

```yaml
resilience4j:
  bulkhead:
    instances:
      reportService:
        max-concurrent-calls: 10
        max-wait-duration: 0
```

### 5. 时间限制器（Time Limiter）

控制方法执行超时。

#### 基础使用

```java
@TimeLimiter(
    name = "searchService",
    timeoutDuration = 3000,
    cancelRunningFuture = true
)
public SearchResult search(SearchQuery query) {
    return searchEngine.search(query);
}
```

#### 配置说明

```yaml
resilience4j:
  time-limiter:
    instances:
      searchService:
        timeout-duration: 3000
        cancel-running-future: true
```

## 组合使用

多个功能可以组合使用，执行顺序：Retry -> CircuitBreaker -> RateLimiter -> TimeLimiter -> Bulkhead

```java
@Service
public class OrderService {
    
    @Retry(name = "orderService", maxAttempts = 3)
    @CircuitBreaker(name = "orderService")
    @RateLimiter(name = "orderService", limitForPeriod = 10)
    @TimeLimiter(name = "orderService", timeoutDuration = 5000)
    @Bulkhead(name = "orderService", maxConcurrentCalls = 5)
    public Order createOrder(OrderRequest request) {
        return remoteOrderService.create(request);
    }
}
```

## 降级策略

支持4种降级策略：

### 1. 抛出异常（EXCEPTION）

```java
@CircuitBreaker(
    name = "userService",
    fallbackStrategy = FallbackStrategy.EXCEPTION
)
public User getUser(Long id) {
    return remoteService.getUser(id);
}
```

### 2. 调用降级方法（METHOD）

```java
@CircuitBreaker(
    name = "userService",
    fallbackStrategy = FallbackStrategy.METHOD,
    fallbackMethod = "getUserFallback"
)
public User getUser(Long id) {
    return remoteService.getUser(id);
}

public User getUserFallback(Long id, Throwable throwable) {
    return User.defaultUser(id);
}
```

### 3. 返回默认值（DEFAULT_VALUE）

```java
@CircuitBreaker(
    name = "countService",
    fallbackStrategy = FallbackStrategy.DEFAULT_VALUE,
    fallbackValue = "0"
)
public int getCount() {
    return remoteService.getCount();
}
```

### 4. 返回 null（NULL）

```java
@CircuitBreaker(
    name = "userService",
    fallbackStrategy = FallbackStrategy.NULL
)
public User getUser(Long id) {
    return remoteService.getUser(id);
}
```

## 完整配置示例

```yaml
resilience4j:
  enabled: true
  
  # 熔断器配置
  circuit-breaker:
    instances:
      userService:
        failure-rate-threshold: 50.0
        slow-call-rate-threshold: 80.0
        slow-call-duration-threshold: 3000
        sliding-window-size: 100
        minimum-number-of-calls: 10
      
      orderService:
        failure-rate-threshold: 60.0
        sliding-window-size: 50
  
  # 限流器配置
  rate-limiter:
    instances:
      apiService:
        limit-for-period: 10
        limit-refresh-period: 1000000000
        timeout-duration: 5000
  
  # 重试配置
  retry:
    instances:
      paymentService:
        max-attempts: 3
        wait-duration: 1000
  
  # 舱壁配置
  bulkhead:
    instances:
      reportService:
        max-concurrent-calls: 10
        max-wait-duration: 0
  
  # 时间限制器配置
  time-limiter:
    instances:
      searchService:
        timeout-duration: 3000
        cancel-running-future: true
```

## 监控指标

启用 Actuator 监控：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,circuitbreakers,ratelimiters,retries,bulkheads,timelimiters
  health:
    circuitbreakers:
      enabled: true
    ratelimiters:
      enabled: true
```

访问端点：
- `/actuator/circuitbreakers` - 熔断器状态
- `/actuator/ratelimiters` - 限流器状态
- `/actuator/retries` - 重试状态
- `/actuator/bulkheads` - 舱壁状态
- `/actuator/timelimiters` - 时间限制器状态

## 使用场景

### 场景1：微服务调用保护

```java
@Service
public class UserService {
    
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService", maxAttempts = 3)
    @TimeLimiter(name = "userService", timeoutDuration = 5000)
    public User getUser(Long id) {
        return userServiceClient.getUser(id);
    }
}
```

### 场景2：API 限流

```java
@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping("/data")
    @RateLimiter(name = "publicApi", limitForPeriod = 100)
    public ResponseEntity<Data> getData() {
        return ResponseEntity.ok(dataService.getData());
    }
}
```

### 场景3：资源密集型操作

```java
@Service
public class ReportService {
    
    @Bulkhead(name = "reportService", maxConcurrentCalls = 5)
    @TimeLimiter(name = "reportService", timeoutDuration = 30000)
    public Report generateReport(ReportRequest request) {
        return reportGenerator.generate(request);
    }
}
```

## 最佳实践

1. **合理设置阈值**：根据实际业务场景调整各项阈值
2. **提供降级方法**：为关键业务提供有意义的降级逻辑
3. **监控告警**：配置监控指标和告警规则
4. **测试验证**：充分测试各种异常场景
5. **文档记录**：记录各个服务的容错配置和降级策略

## 许可证

Apache License 2.0
