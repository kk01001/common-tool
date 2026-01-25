# Resilience4j 示例项目

这是一个完整的 Resilience4j Spring Boot Starter 示例项目，展示了所有核心功能的使用。

## 功能演示

### 1. 熔断器 (Circuit Breaker)
- 自动检测服务故障并触发熔断
- 失败率超过50%时熔断
- 提供降级方法

### 2. 限流器 (Rate Limiter)
- 控制请求速率
- 每秒最多5个请求
- 超出限制时触发降级

### 3. 重试 (Retry)
- 失败后自动重试
- 最多重试3次
- 重试间隔500ms

### 4. 舱壁 (Bulkhead)
- 限制并发调用数
- 最多3个并发请求
- 超出限制时触发降级

### 5. 时间限制器 (Time Limiter)
- 超时控制
- 超过2秒自动超时
- 提供降级方法

### 6. 组合使用
- 同时使用多个功能
- 执行顺序：Retry -> CircuitBreaker -> RateLimiter

## 快速开始

### 1. 启动应用

```bash
mvn spring-boot:run
```

### 2. 访问页面

打开浏览器访问：http://localhost:8090

### 3. 测试功能

页面提供了交互式界面，可以测试所有功能：

- **熔断器测试**：调整失败率，观察熔断效果
- **限流器测试**：快速点击，观察限流效果
- **重试测试**：设置高失败率，观察重试过程
- **舱壁测试**：并发测试，观察并发控制
- **时间限制器测试**：设置长延迟，观察超时效果

## API 端点

### 测试端点

- `GET /api/resilience/circuit-breaker?failureRate=50` - 测试熔断器
- `GET /api/resilience/rate-limiter` - 测试限流器
- `GET /api/resilience/retry?failureRate=70` - 测试重试
- `GET /api/resilience/bulkhead?delayMs=1000` - 测试舱壁
- `GET /api/resilience/time-limiter?delayMs=3000` - 测试时间限制器
- `GET /api/resilience/combined?failureRate=30` - 测试组合功能

### 管理端点

- `POST /api/resilience/reset` - 重置计数器
- `GET /api/resilience/stats` - 获取统计信息

### 监控端点

- `GET /actuator/health` - 健康检查
- `GET /actuator/circuitbreakers` - 熔断器状态
- `GET /actuator/ratelimiters` - 限流器状态
- `GET /actuator/retries` - 重试状态
- `GET /actuator/bulkheads` - 舱壁状态
- `GET /actuator/timelimiters` - 时间限制器状态

## 配置说明

配置文件位于 `src/main/resources/application.yml`

```yaml
resilience4j:
  circuit-breaker:
    instances:
      testCircuitBreaker:
        failure-rate-threshold: 50.0  # 失败率阈值
        minimum-number-of-calls: 5    # 最小调用次数
  
  rate-limiter:
    instances:
      testRateLimiter:
        limit-for-period: 5           # 每周期允许请求数
        limit-refresh-period: 1000000000  # 周期（纳秒）
  
  retry:
    instances:
      testRetry:
        max-attempts: 3               # 最大重试次数
        wait-duration: 500            # 重试间隔（毫秒）
```

## 测试场景

### 场景1：熔断器测试

1. 设置失败率为 50%
2. 多次点击"测试熔断器"
3. 观察当失败次数达到阈值时，熔断器打开
4. 后续请求直接返回降级响应

### 场景2：限流器测试

1. 点击"批量测试"按钮
2. 观察前5个请求成功，后续请求被限流
3. 等待1秒后，限流器重置

### 场景3：重试测试

1. 设置失败率为 70%
2. 点击"测试重试"
3. 观察日志中的重试过程
4. 最多重试3次后返回降级响应

### 场景4：舱壁测试

1. 设置延迟时间为 2000ms
2. 点击"并发测试"
3. 观察前3个请求正常处理
4. 第4、5个请求被拒绝（超出并发限制）

### 场景5：时间限制器测试

1. 设置延迟时间为 3000ms（超过2秒超时限制）
2. 点击"测试时间限制器"
3. 观察请求超时，返回降级响应

## 注意事项

1. 熔断器需要达到最小调用次数才会生效
2. 限流器的周期是固定的，不会因为请求而重置
3. 重试会增加实际调用次数
4. 舱壁的并发限制是全局的
5. 时间限制器的超时时间包括方法执行时间

## 技术栈

- Spring Boot 3.x
- Resilience4j 2.2.0
- Hutool 5.8.x

## 许可证

Apache License 2.0
