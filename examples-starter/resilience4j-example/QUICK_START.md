# Resilience4j 示例 - 快速开始

## 一键启动

### 方式1：使用 Maven

```bash
cd examples-starter/resilience4j-example
mvn spring-boot:run
```

### 方式2：使用 IDE

1. 在 IDE 中打开项目
2. 找到 `Resilience4jExampleApplication` 类
3. 右键运行

## 访问应用

启动成功后，打开浏览器访问：

**主页面**: http://localhost:8090

**监控端点**:
- http://localhost:8090/actuator/health
- http://localhost:8090/actuator/circuitbreakers
- http://localhost:8090/actuator/ratelimiters

## 5分钟快速体验

### 1. 测试熔断器

1. 打开主页面
2. 找到"熔断器"卡片
3. 设置失败率为 50%
4. 连续点击"测试熔断器"按钮 10 次
5. 观察：前几次可能成功或失败，当失败次数达到阈值后，熔断器打开，所有请求返回降级响应

### 2. 测试限流器

1. 找到"限流器"卡片
2. 点击"批量测试 (10次)"按钮
3. 观察：前5个请求成功，后5个请求被限流（配置为每秒5个请求）

### 3. 测试重试

1. 找到"重试"卡片
2. 设置失败率为 70%
3. 点击"测试重试"按钮
4. 打开浏览器控制台查看日志
5. 观察：失败后会自动重试，最多3次

### 4. 测试舱壁

1. 找到"舱壁"卡片
2. 设置延迟时间为 2000ms
3. 点击"并发测试 (5次)"按钮
4. 观察：前3个请求正常处理，后2个请求被拒绝（配置为最多3个并发）

### 5. 测试时间限制器

1. 找到"时间限制器"卡片
2. 设置延迟时间为 3000ms
3. 点击"测试时间限制器"按钮
4. 观察：请求超时（配置为2秒超时），返回降级响应

## 查看监控指标

访问 Actuator 端点查看详细指标：

```bash
# 熔断器状态
curl http://localhost:8090/actuator/circuitbreakers

# 限流器状态
curl http://localhost:8090/actuator/ratelimiters

# 重试状态
curl http://localhost:8090/actuator/retries

# 舱壁状态
curl http://localhost:8090/actuator/bulkheads

# 时间限制器状态
curl http://localhost:8090/actuator/timelimiters
```

## 常见问题

### Q: 熔断器为什么没有生效？

A: 熔断器需要达到最小调用次数（默认5次）才会开始计算失败率。多点击几次测试按钮。

### Q: 限流器测试时为什么有些请求成功了？

A: 限流器是基于时间窗口的，每秒重置一次。如果点击间隔超过1秒，限流器会重置。

### Q: 如何重置所有统计数据？

A: 点击页面底部的"重置统计"按钮，或者重启应用。

## 下一步

- 查看 [完整文档](README.md)
- 修改 `application.yml` 调整配置参数
- 查看源码了解实现细节
