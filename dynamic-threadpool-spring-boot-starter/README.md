# Dynamic ThreadPool Spring Boot Starter

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![JDK](https://img.shields.io/badge/JDK-21-green.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 项目介绍

Dynamic ThreadPool 是一个强大的 Spring Boot Starter，提供线程池的动态管理、监控和告警能力。无需重启应用即可动态调整线程池参数，并通过 Actuator 端点和 Micrometer 指标实现全面监控。

## 核心功能

- ✅ **动态参数调整**：运行时动态修改核心线程数、最大线程数、队列容量等参数
- ✅ **全面监控**：实时监控活跃线程数、队列大小、完成任务数、拒绝次数等指标
- ✅ **告警机制**：支持队列溢出、拒绝率、活跃线程比例等多维度告警
- ✅ **Actuator 集成**：提供 REST API 查询和更新线程池配置
- ✅ **指标导出**：集成 Micrometer，支持 Prometheus、Grafana 等监控系统
- ✅ **注解驱动**：通过 `@DynamicThreadPool` 注解声明式创建线程池
- ✅ **配置刷新**：支持配置中心（Nacos/Apollo）动态刷新
- ✅ **优雅关闭**：应用停止时优雅关闭所有线程池

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>dynamic-threadpool-spring-boot-starter</artifactId>
    <version>2.4.8</version>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中添加配置：

```yaml
dynamic-threadpool:
  enabled: true
  
  # 全局默认配置
  global:
    corePoolSize: 5
    maxPoolSize: 10
    queueCapacity: 100
    keepAliveTime: 60s
    threadNamePrefix: dynamic-pool-
    allowCoreThreadTimeout: false
  
  # 具体线程池配置
  pools:
    order-pool:
      corePoolSize: 10
      maxPoolSize: 20
      queueCapacity: 200
      threadNamePrefix: order-
    
    async-pool:
      corePoolSize: 5
      maxPoolSize: 15
      queueCapacity: 50
      threadNamePrefix: async-
  
  # 监控配置
  monitor:
    enabled: true
    enableActuator: true
    enableMetrics: true
    collectInterval: 5s
  
  # 告警配置
  alarm:
    enabled: true
    queueUsageThreshold: 0.8        # 队列使用率 80%
    activeThreadRatioThreshold: 0.9  # 活跃线程比例 90%
    rejectCountThreshold: 100        # 拒绝次数 100
    alarmInterval: 5m                # 告警间隔 5 分钟
```

### 3. 使用注解创建线程池

```java
@Configuration
public class ThreadPoolConfig {
    
    @Bean
    @DynamicThreadPool(
        poolName = "business-pool",
        corePoolSize = 10,
        maxPoolSize = 20,
        queueCapacity = 100,
        keepAliveSeconds = 60,
        threadNamePrefix = "business-"
    )
    public Executor businessThreadPool() {
        return null; // Starter 会自动创建
    }
}
```

### 4. 使用线程池

```java
@Service
public class OrderService {
    
    @Autowired
    @Qualifier("business-pool")
    private Executor businessThreadPool;
    
    public void processOrder(Order order) {
        businessThreadPool.execute(() -> {
            // 业务逻辑
            System.out.println("Processing order: " + order.getId());
        });
    }
}
```

## 高级用法

### 通过编程方式创建线程池

```java
@Service
public class DynamicPoolService {
    
    @Autowired
    private ThreadPoolRegistry registry;
    
    @Autowired
    private ThreadPoolFactory factory;
    
    public void createCustomPool() {
        ThreadPoolConfig config = ThreadPoolConfig.builder()
                .poolName("custom-pool")
                .corePoolSize(5)
                .maxPoolSize(10)
                .queueCapacity(50)
                .keepAliveTime(Duration.ofSeconds(30))
                .threadNamePrefix("custom-")
                .build();
        
        DynamicThreadPoolWrapper wrapper = factory.createThreadPool("custom-pool", config);
        registry.register("custom-pool", wrapper);
    }
}
```

### 动态更新配置

```java
@Service
public class PoolManagementService {
    
    @Autowired
    private ThreadPoolRegistry registry;
    
    public void updatePoolConfig(String poolName) {
        DynamicThreadPoolWrapper wrapper = registry.getThreadPool(poolName);
        
        ThreadPoolConfig newConfig = ThreadPoolConfig.builder()
                .poolName(poolName)
                .corePoolSize(20)  // 增加核心线程数
                .maxPoolSize(30)   // 增加最大线程数
                .queueCapacity(200)
                .keepAliveTime(wrapper.getConfig().getKeepAliveTime())
                .queueType(wrapper.getConfig().getQueueType())
                .threadNamePrefix(wrapper.getConfig().getThreadNamePrefix())
                .rejectedPolicyType(wrapper.getConfig().getRejectedPolicyType())
                .allowCoreThreadTimeout(wrapper.getConfig().getAllowCoreThreadTimeout())
                .build();
        
        wrapper.updateConfig(newConfig);
    }
}
```

### 监控指标

```java
@Service
public class MonitorService {
    
    @Autowired
    private ThreadPoolMonitor monitor;
    
    public void printMetrics() {
        Map<String, ThreadPoolMetrics> metricsMap = monitor.getAllMetrics();
        
        metricsMap.forEach((poolName, metrics) -> {
            System.out.println("Pool: " + poolName);
            System.out.println("Active Threads: " + metrics.getActiveThreadCount());
            System.out.println("Queue Size: " + metrics.getQueueSize());
            System.out.println("Completed Tasks: " + metrics.getCompletedTaskCount());
            System.out.println("Reject Count: " + metrics.getRejectCount());
            System.out.println("Queue Usage: " + String.format("%.2f%%", metrics.getQueueUsageRatio() * 100));
        });
    }
}
```

## Actuator 端点

启用 Actuator 后，可通过 HTTP 接口管理线程池。

### 查询所有线程池

```bash
GET http://localhost:8080/actuator/threadpool
```

响应示例：
```json
{
  "total": 2,
  "pools": {
    "order-pool": {
      "poolName": "order-pool",
      "corePoolSize": 10,
      "maxPoolSize": 20,
      "activeThreadCount": 5,
      "queueSize": 12,
      "completedTaskCount": 1523,
      "rejectCount": 0,
      "queueUsageRatio": 0.06
    }
  }
}
```

### 查询指定线程池

```bash
GET http://localhost:8080/actuator/threadpool/{poolName}
```

### 更新线程池配置

```bash
POST http://localhost:8080/actuator/threadpool/{poolName}
Content-Type: application/json

{
  "corePoolSize": 15,
  "maxPoolSize": 25,
  "queueCapacity": 300
}
```

## Micrometer 指标

集成 Micrometer 后，可在 Prometheus、Grafana 等监控系统中查看以下指标：

| 指标名称 | 说明 |
|---------|------|
| `threadpool.core.size` | 核心线程数 |
| `threadpool.max.size` | 最大线程数 |
| `threadpool.pool.size` | 当前线程池大小 |
| `threadpool.active.count` | 活跃线程数 |
| `threadpool.queue.size` | 队列大小 |
| `threadpool.queue.capacity` | 队列容量 |
| `threadpool.queue.usage.ratio` | 队列使用率 |
| `threadpool.active.ratio` | 活跃线程比例 |
| `threadpool.completed.task.count` | 已完成任务数 |
| `threadpool.task.count` | 总任务数 |
| `threadpool.reject.count` | 拒绝次数 |

## 告警扩展

可通过继承 `ThreadPoolAlarmHandler` 实现自定义告警通知：

```java
@Component
public class CustomAlarmHandler extends ThreadPoolAlarmHandler {
    
    @Autowired
    private RobotClient robotClient;  // 使用 robot-message-starter
    
    public CustomAlarmHandler(ThreadPoolRegistry registry, 
                              DynamicThreadPoolProperties properties) {
        super(registry, properties);
    }
    
    @Override
    protected void handleAlarm(AlarmEvent event) {
        super.handleAlarm(event);
        
        // 发送钉钉告警
        TextMessage message = new TextMessage();
        message.setContent(String.format(
            "【线程池告警】\n" +
            "线程池：%s\n" +
            "告警类型：%s\n" +
            "当前值：%.2f\n" +
            "阈值：%.2f",
            event.getPoolName(),
            event.getAlarmType(),
            event.getCurrentValue(),
            event.getThreshold()
        ));
        robotClient.sendMessage(message);
    }
}
```

## 配置刷新（Nacos 集成）

如果项目使用 Nacos 配置中心，可实现配置热更新：

```java
@Component
public class NacosConfigRefreshListener {
    
    @Autowired
    private ThreadPoolRefreshHandler refreshHandler;
    
    @NacosConfigListener(dataId = "threadpool-config", groupId = "DEFAULT_GROUP")
    public void onConfigChange(String newConfig) {
        // 解析新配置并刷新
        refreshHandler.refreshAllConfigurations();
    }
}
```

## 最佳实践

### 1. 线程池命名规范

- 使用有意义的线程池名称，便于监控和排查问题
- 线程名称前缀应包含业务特征：`order-`, `payment-`, `notification-`

### 2. 参数配置建议

- **核心线程数**：根据 CPU 核数和业务特点设置，CPU 密集型任务可设为 `CPU核数 + 1`
- **最大线程数**：通常设为核心线程数的 2-3 倍
- **队列容量**：根据业务峰值和内存限制设置，避免过大导致 OOM
- **拒绝策略**：默认 `AbortPolicy`，关键业务可使用 `CallerRunsPolicy`

### 3. 监控告警

- 设置合理的告警阈值，避免告警风暴
- 队列使用率建议设为 80%，活跃线程比例建议设为 90%
- 集成钉钉、企业微信等及时通知相关人员

### 4. 优雅关闭

应用停止时，线程池会自动优雅关闭。如需自定义行为：

```java
@Component
public class GracefulShutdown {
    
    @Autowired
    private ThreadPoolRegistry registry;
    
    @PreDestroy
    public void shutdown() {
        registry.getAllThreadPools().forEach(wrapper -> {
            try {
                wrapper.shutdown();
                wrapper.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                wrapper.shutdownNow();
            }
        });
    }
}
```

## 配置参数说明

### 全局配置

| 参数 | 说明 | 默认值 |
|-----|------|--------|
| `enabled` | 是否启用动态线程池 | true |
| `global.corePoolSize` | 默认核心线程数 | 5 |
| `global.maxPoolSize` | 默认最大线程数 | 10 |
| `global.queueCapacity` | 默认队列容量 | 100 |
| `global.keepAliveTime` | 默认线程存活时间 | 60s |
| `global.threadNamePrefix` | 默认线程名称前缀 | dynamic-pool- |
| `global.allowCoreThreadTimeout` | 是否允许核心线程超时 | false |

### 监控配置

| 参数 | 说明 | 默认值 |
|-----|------|--------|
| `monitor.enabled` | 是否启用监控 | true |
| `monitor.enableActuator` | 是否启用 Actuator 端点 | true |
| `monitor.enableMetrics` | 是否启用 Micrometer 指标 | true |
| `monitor.collectInterval` | 监控采集间隔 | 5s |

### 告警配置

| 参数 | 说明 | 默认值 |
|-----|------|--------|
| `alarm.enabled` | 是否启用告警 | true |
| `alarm.queueUsageThreshold` | 队列使用率阈值 (0-1) | 0.8 |
| `alarm.activeThreadRatioThreshold` | 活跃线程比例阈值 (0-1) | 0.9 |
| `alarm.rejectCountThreshold` | 拒绝次数阈值 | 100 |
| `alarm.alarmInterval` | 告警间隔时间 | 5m |

## 常见问题

### Q: 如何查看当前有哪些线程池？

A: 访问 `http://localhost:8080/actuator/threadpool` 或注入 `ThreadPoolRegistry` 调用 `getAllPoolNames()`

### Q: 动态修改配置后会丢失任务吗？

A: 不会。配置更新不会影响正在执行的任务和队列中的任务。

### Q: 可以在不重启应用的情况下调整队列容量吗？

A: 队列容量在线程池创建时确定，无法动态修改。建议预留足够的容量或使用无界队列（需注意内存风险）。

### Q: 告警会不会太频繁？

A: 内置了告警节流机制，相同类型的告警在 `alarmInterval` 时间内只会触发一次。

### Q: 如何与 Nacos/Apollo 集成实现配置热更新？

A: 监听配置变更事件，调用 `ThreadPoolRefreshHandler.refreshAllConfigurations()` 即可。

## 许可证

Apache License 2.0

## 作者

kk01001

## 参考项目

- [DynamicTp](https://github.com/dromara/dynamic-tp)
- [Hippo4j](https://github.com/opengoofy/hippo4j)
