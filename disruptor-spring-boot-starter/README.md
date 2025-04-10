# Disruptor Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/disruptor-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.kk01001/disruptor-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 将高性能事件处理框架 Disruptor 与 Spring Boot 无缝集成，支持注解驱动的事件处理、自动配置、性能监控，轻松构建高吞吐量、低延迟的事件驱动应用。

## 背景

在高性能系统开发中，事件处理和消息传递是常见的需求。传统的消息队列虽然功能丰富，但往往面临性能瓶颈。LMAX Disruptor 作为一个高性能的事件处理框架，通过无锁设计和高效的内存使用模式，提供了卓越的性能表现，适用于对延迟和吞吐量有极高要求的场景。

`disruptor-spring-boot-starter` 项目旨在简化 Disruptor 在 Spring Boot 应用中的使用，让开发者通过简单的注解和配置即可利用 Disruptor 的强大能力，而无需关注底层复杂的实现细节。

## 项目目标

- **简化配置**：提供自动配置和默认值，减少样板代码
- **注解驱动**：支持通过简单的注解定义和使用 Disruptor 队列
- **性能监控**：集成 Micrometer，提供运行时监控能力
- **Java 21 支持**：充分利用虚拟线程，进一步提升性能
- **降低学习成本**：屏蔽 Disruptor 的复杂性，提供简单易用的 API

## 核心功能与亮点 ✨

- **注解式监听器**：通过 `@DisruptorListener` 注解快速定义事件处理方法
- **模板化发送**：使用 `DisruptorTemplate` 简化事件发布
- **多种等待策略**：支持 Blocking、Yielding、Sleeping 等多种等待策略
- **生产者类型配置**：支持单生产者和多生产者模式
- **监控指标**：集成 Micrometer，提供队列大小、消费速率等指标
- **虚拟线程支持**：利用 Java 21 虚拟线程，提升性能和资源利用率
- **自动装配**：基于 Spring Boot 自动配置机制，开箱即用

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- LMAX Disruptor 4.0.0
- Micrometer
- Lombok

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>disruptor-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 配置属性

在 `application.yml` 或 `application.properties` 中添加配置（可选，有默认值）：

```yaml
disruptor:
  buffer-size: 1024             # 缓冲区大小
  producer-type: MULTI          # 生产者类型：SINGLE 或 MULTI
  wait-strategy: BLOCKING       # 等待策略：BLOCKING、YIELDING、SLEEPING 等
  enable-metrics: true          # 是否启用监控指标
```

### 使用方式

#### 1. 创建事件监听器

使用 `@DisruptorListener` 注解标记事件处理方法：

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 订单事件处理器
 */
@Component
public class OrderEventHandler {

    @DisruptorListener(
        value = "orderQueue",              // 队列名称
        bufferSize = 2048,                 // 缓冲区大小
        producerType = ProducerType.MULTI, // 生产者类型
        waitStrategy = WaitStrategyType.YIELDING // 等待策略
    )
    public void handleOrderEvent(OrderEvent event) {
        // 处理订单事件
        System.out.println("处理订单：" + event.getOrderId());
    }
}
```

#### 2. 发送事件

注入 `DisruptorTemplate` 并发送事件：

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 订单服务
 */
@Service
public class OrderService {

    private final DisruptorTemplate disruptorTemplate;

    public OrderService(DisruptorTemplate disruptorTemplate) {
        this.disruptorTemplate = disruptorTemplate;
    }

    public void createOrder(String orderId, BigDecimal amount) {
        // 创建订单...

        // 发布订单创建事件
        OrderEvent event = new OrderEvent(orderId, amount);
        disruptorTemplate.send("orderQueue", event);
    }
}
```

#### 3. 手动创建和管理队列

除了使用注解外，还可以手动创建和管理队列：

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 手动创建队列示例
 */
@Configuration
public class DisruptorConfig {

    @Bean
    public MessageHandler<PaymentEvent> paymentHandler() {
        return event -> {
            // 处理支付事件
            System.out.println("处理支付: " + event.getPaymentId());
        };
    }

    @Bean
    public Disruptor<DisruptorEvent<PaymentEvent>> paymentDisruptor(
            DisruptorTemplate disruptorTemplate,
            MessageHandler<PaymentEvent> paymentHandler) {
        
        ThreadFactory threadFactory = Thread.ofVirtual()
                .name("payment-handler-", 0)
                .factory();
        
        return disruptorTemplate.createQueue(
                "paymentQueue",
                1024,
                ProducerType.MULTI,
                new YieldingWaitStrategy(),
                threadFactory,
                paymentHandler
        );
    }
}
```

## 性能监控

启用监控指标后（`disruptor.enable-metrics=true`），会自动注册以下指标：

- `disruptor.queue.size`：队列大小
- `disruptor.queue.capacity`：队列容量
- `disruptor.event.count`：处理的事件计数
- `disruptor.event.time`：事件处理时间

可通过 Spring Boot Actuator 和 Prometheus 进行监控。

## 等待策略

Disruptor 提供多种等待策略，可根据场景选择：

| 策略名称 | 说明 | 适用场景 |
|---------|------|---------|
| BLOCKING | 阻塞等待 | 低延迟、CPU占用敏感场景 |
| YIELDING | 自旋让出CPU | 低延迟、CPU占用不敏感场景 |
| SLEEPING | 自旋后睡眠 | 平衡延迟和CPU占用 |
| BUSY_SPIN | 忙等待自旋 | 超低延迟、CPU占用不敏感场景 |
| TIMEOUT_BLOCKING | 带超时的阻塞 | 需要定时唤醒处理 |

## 高级配置

### 自定义线程工厂

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 自定义线程工厂
 */
@Configuration
public class ThreadFactoryConfig {

    @Bean
    public ThreadFactory disruptorThreadFactory() {
        return Thread.ofPlatform()  // 使用平台线程而非虚拟线程
                .name("custom-disruptor-", 0)
                .daemon(true)
                .priority(Thread.MAX_PRIORITY)
                .factory();
    }
}
```

### 异常处理

```java
/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 异常处理示例
 */
@Component
public class PaymentHandler implements MessageHandler<PaymentEvent> {

    @Override
    public void onEvent(PaymentEvent event) {
        try {
            // 处理支付事件
            processPayment(event);
        } catch (Exception e) {
            // 记录异常并进行恢复处理
            log.error("支付处理失败: {}", event.getPaymentId(), e);
        }
    }
}
```

## 最佳实践

1. **合理设置缓冲区大小**：缓冲区大小必须是2的幂，根据内存和性能需求设置。
2. **选择合适的等待策略**：低延迟场景选择YIELDING或BUSY_SPIN，平衡CPU和延迟选择SLEEPING。
3. **单生产者优化**：如果确定只有一个生产者，使用SINGLE生产者类型可获得更好性能。
4. **避免阻塞消费者**：消费者应快速处理事件，避免长时间阻塞。
5. **合理划分队列**：不同类型、不同处理速度的事件应使用不同队列。
6. **监控队列大小**：定期监控队列使用情况，及时调整缓冲区大小。

## 性能对比

与传统消息队列相比，Disruptor 提供显著的性能优势：

| 方案 | 吞吐量 (ops/s) | 延迟 (P99) |
|-----|---------------|-----------|
| Disruptor | 25,000,000+ | < 100ns |
| ArrayBlockingQueue | 5,000,000+ | < 1μs |
| LinkedBlockingQueue | 2,000,000+ | < 10μs |
| Kafka | 1,000,000+ | < 10ms |

> 注：实际性能取决于硬件配置和使用场景

## 应用场景

- **高性能交易系统**：金融交易、订单处理
- **实时数据处理**：日志聚合、指标计算
- **线程间高效通信**：生产者-消费者模式
- **事件驱动架构**：低延迟事件处理
- **流量削峰**：处理突发流量
- **异步处理管道**：多阶段数据处理

## 贡献 🙏

欢迎提交 Issue 或 Pull Request 参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。 