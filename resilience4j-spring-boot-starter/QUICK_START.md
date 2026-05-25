# Resilience4j 快速开始

## 5分钟快速上手

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>resilience4j-spring-boot-starter</artifactId>
    <version>2.5.0</version>
</dependency>
```

### 2. 选择你需要的功能

#### 熔断器 - 防止故障扩散

```java
@Service
public class UserService {
    
    @CircuitBreaker(name = "userService")
    public User getUser(Long id) {
        return remoteService.getUser(id);
    }
}
```

#### 限流器 - 控制请求速率

```java
@Service
public class ApiService {
    
    @RateLimiter(name = "apiService", limitForPeriod = 10)
    public ApiResponse callApi(ApiRequest request) {
        return apiClient.call(request);
    }
}
```

#### 重试 - 失败自动重试

```java
@Service
public class PaymentService {
    
    @Retry(name = "paymentService", maxAttempts = 3)
    public PaymentResult pay(PaymentRequest request) {
        return paymentGateway.pay(request);
    }
}
```

#### 舱壁 - 限制并发数

```java
@Service
public class ReportService {
    
    @Bulkhead(name = "reportService", maxConcurrentCalls = 5)
    public Report generateReport(Long id) {
        return reportGenerator.generate(id);
    }
}
```

#### 时间限制器 - 超时控制

```java
@Service
public class SearchService {
    
    @TimeLimiter(name = "searchService", timeoutDuration = 3000)
    public SearchResult search(SearchQuery query) {
        return searchEngine.search(query);
    }
}
```

### 3. 添加降级逻辑（可选）

```java
@Service
public class UserService {
    
    @CircuitBreaker(
        name = "userService",
        fallbackStrategy = FallbackStrategy.METHOD,
        fallbackMethod = "getUserFallback"
    )
    public User getUser(Long id) {
        return remoteService.getUser(id);
    }
    
    // 降级方法
    public User getUserFallback(Long id, Throwable throwable) {
        log.error("获取用户失败，使用降级逻辑", throwable);
        return User.defaultUser(id);
    }
}
```

### 4. 配置参数（可选）

```yaml
resilience4j:
  circuit-breaker:
    instances:
      userService:
        failure-rate-threshold: 50.0  # 失败率50%触发熔断
        minimum-number-of-calls: 10   # 最少10次调用
  
  rate-limiter:
    instances:
      apiService:
        limit-for-period: 10          # 每秒10个请求
  
  retry:
    instances:
      paymentService:
        max-attempts: 3               # 最多重试3次
        wait-duration: 1000           # 重试间隔1秒
```

## 组合使用

多个功能可以组合使用：

```java
@Service
public class OrderService {
    
    @Retry(name = "orderService", maxAttempts = 3)
    @CircuitBreaker(name = "orderService")
    @RateLimiter(name = "orderService", limitForPeriod = 10)
    public Order createOrder(OrderRequest request) {
        return remoteOrderService.create(request);
    }
}
```

## 常见场景

### 场景1：调用第三方API

```java
@CircuitBreaker(name = "thirdPartyApi")
@Retry(name = "thirdPartyApi", maxAttempts = 3)
@TimeLimiter(name = "thirdPartyApi", timeoutDuration = 5000)
public ApiResponse callThirdPartyApi(ApiRequest request) {
    return thirdPartyClient.call(request);
}
```

### 场景2：高并发接口

```java
@RateLimiter(name = "publicApi", limitForPeriod = 100)
@Bulkhead(name = "publicApi", maxConcurrentCalls = 50)
public Data getData() {
    return dataService.getData();
}
```

### 场景3：耗时操作

```java
@Bulkhead(name = "reportService", maxConcurrentCalls = 5)
@TimeLimiter(name = "reportService", timeoutDuration = 30000)
public Report generateReport(ReportRequest request) {
    return reportGenerator.generate(request);
}
```

## 下一步

- 查看 [完整文档](README.md) 了解所有功能
- 配置监控端点查看运行状态
- 根据实际业务调整参数
