# Dynamic MQ Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/dynamic-mq-spring-boot-starter.svg)](https://search.maven.org/artifact/io.github.kk01001/dynamic-mq-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java Version](https://img.shields.io/badge/Java-21+-green.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 📖 项目介绍

Dynamic MQ Spring Boot Starter 是一个支持多种消息队列的统一框架，提供了一套统一的API来操作不同的MQ中间件。通过策略模式和模板方法模式，实现了RocketMQ、RabbitMQ、Kafka、Redis等MQ的动态切换和统一管理。

### 🎯 核心特性

- **🔄 动态切换**：运行时无缝切换不同MQ类型，无需重启应用
- **🎭 统一API**：提供一套统一的生产者和消费者API，屏蔽底层MQ差异
- **⚡ 完整功能**：支持同步/异步/oneway/顺序/延迟/事务消息
- **🏗️ 架构清晰**：基于策略模式和模板方法模式的可扩展架构
- **🔧 易于集成**：Spring Boot Starter方式，开箱即用
- **📊 监控支持**：集成Spring Boot Actuator健康检查
- **🎨 灵活配置**：支持多种消费者注册方式（注解和手动注册）

### 🚀 支持的MQ类型

| MQ类型 | 同步消息 | 异步消息 | OneWay | 顺序消息 | 延迟消息 | 事务消息 | 批量消息 |
|--------|---------|---------|--------|---------|---------|---------|---------|
| **RocketMQ** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **RabbitMQ** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Kafka** | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| **Redis** | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |

## 🏗️ 架构设计

### 核心架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
├─────────────────────────────────────────────────────────────┤
│                  DynamicMqManager                          │
│              (统一入口 & 动态切换)                           │
├─────────────────────────────────────────────────────────────┤
│  DynamicMqProducerManager  │  DynamicMqConsumerManager     │
│      (生产者管理器)         │       (消费者管理器)           │
├─────────────────────────────────────────────────────────────┤
│  MqProducerFactory         │  MqConsumerFactory            │
│    (生产者工厂)             │     (消费者工厂)               │
├─────────────────────────────────────────────────────────────┤
│  RocketMQ │ RabbitMQ │ Kafka │ Redis                       │
│  Producer │ Producer │Producer│Producer                     │
├─────────────────────────────────────────────────────────────┤
│  RocketMQ │ RabbitMQ │ Kafka │ Redis                       │
│  Consumer │ Consumer │Consumer│Consumer                     │
└─────────────────────────────────────────────────────────────┘
```

### 设计模式

1. **策略模式**：不同MQ实现作为不同策略，可动态切换
2. **模板方法模式**：抽象基类定义通用流程，子类实现具体逻辑
3. **工厂模式**：统一创建和管理MQ实例
4. **适配器模式**：统一不同MQ的API差异

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>dynamic-mq-spring-boot-starter</artifactId>
    <version>dev-2.4.6.5</version>
</dependency>
```

### 2. 配置文件

```yaml
dynamic:
  mq:
    # 当前使用的MQ类型
    type: rocketmq

    # 生产者配置
    producer:
      send-timeout: 3000
      max-retry-times: 3
      transaction-enabled: true

    # 消费者配置
    consumer:
      group-name: default-group
      max-retry-times: 3
      consume-timeout: 15000

    # RocketMQ配置
    rocketmq:
      name-server: localhost:9876
      access-key: your-access-key
      secret-key: your-secret-key

    # RabbitMQ配置
    rabbitmq:
      host: localhost
      port: 5672
      username: guest
      password: guest
      virtual-host: /

    # Kafka配置
    kafka:
      bootstrap-servers: localhost:9092
      properties:
        security.protocol: PLAINTEXT

    # Redis配置
    redis:
      host: localhost
      port: 6379
      database: 0
```

### 3. 基本使用

#### 发送消息

```java
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final DynamicMqManager mqManager;

    @PostMapping("/send")
    public String sendMessage(@RequestBody MessageRequest request) {
        // 构建消息
        MqMessage message = MqMessage.builder()
                .topic(request.getTopic())
                .tag(request.getTag())
                .payload(request.getPayload())
                .build();

        // 发送消息
        boolean success = mqManager.send(message);
        return success ? "发送成功" : "发送失败";
    }

    @PostMapping("/send-async")
    public String sendAsyncMessage(@RequestBody MessageRequest request) {
        MqMessage message = MqUtils.buildMessage(
            request.getTopic(),
            request.getTag(),
            request.getPayload()
        );

        // 异步发送
        mqManager.sendAsync(message, new MqSendCallback() {
            @Override
            public void onSuccess(MqSendResult result) {
                log.info("异步发送成功: {}", result);
            }

            @Override
            public void onException(Throwable e) {
                log.error("异步发送失败", e);
            }
        });

        return "异步发送中...";
    }
}
```

## 🔥 高级功能

### 事务消息

```java
@Service
@RequiredArgsConstructor
public class TransactionMessageService {

    private final DynamicMqManager mqManager;

    public void sendTransactionMessage() {
        MqMessage message = MqUtils.buildMessage("order-topic", "create", orderData);

        // 发送事务消息
        mqManager.sendTransaction(message, "tx-001", new MqTransactionListener() {
            @Override
            public TransactionState executeLocalTransaction(MqMessage msg, String transactionId) {
                try {
                    // 执行本地事务
                    orderService.createOrder(orderData);
                    return TransactionState.COMMIT;
                } catch (Exception e) {
                    return TransactionState.ROLLBACK;
                }
            }

            @Override
            public TransactionState checkLocalTransaction(MqMessage msg, String transactionId) {
                // 检查本地事务状态
                return orderService.checkOrderExists(transactionId) ?
                    TransactionState.COMMIT : TransactionState.ROLLBACK;
            }
        });
    }
}
```

### 顺序消息

```java
@Service
@RequiredArgsConstructor
public class OrderlyMessageService {

    private final DynamicMqManager mqManager;

    public void sendOrderlyMessage() {
        MqMessage message = MqUtils.buildMessage("order-topic", "update", orderData);

        // 发送顺序消息，使用订单ID作为顺序键
        mqManager.sendOrderly(message, String.valueOf(orderData.getOrderId()));
    }
}
```

### 延迟消息

```java
@Service
@RequiredArgsConstructor
public class DelayMessageService {

    private final DynamicMqManager mqManager;

    public void sendDelayMessage() {
        MqMessage message = MqMessage.builder()
                .topic("delay-topic")
                .tag("delay-tag")
                .payload("延迟消息内容")
                .delayTime(30000L) // 延迟30秒
                .build();

        mqManager.send(message);
    }
}
```

### 批量消息

```java
@Service
@RequiredArgsConstructor
public class BatchMessageService {

    private final DynamicMqManager mqManager;

    public void sendBatchMessages() {
        MqMessage[] messages = {
            MqUtils.buildMessage("batch-topic", "tag1", "消息1"),
            MqUtils.buildMessage("batch-topic", "tag2", "消息2"),
            MqUtils.buildMessage("batch-topic", "tag3", "消息3")
        };

        // 批量发送
        boolean success = mqManager.sendBatch(messages);
        log.info("批量发送结果: {}", success);
    }
}
```

### 动态切换MQ

```java
@RestController
@RequiredArgsConstructor
public class MqSwitchController {

    private final DynamicMqManager mqManager;

    @PostMapping("/switch/{mqType}")
    public String switchMqType(@PathVariable String mqType) {
        try {
            MqType type = MqType.valueOf(mqType.toUpperCase());
            mqManager.switchMqType(type);
            return "切换到 " + mqType + " 成功";
        } catch (Exception e) {
            return "切换失败: " + e.getMessage();
        }
    }

    @GetMapping("/current-type")
    public String getCurrentMqType() {
        return mqManager.getCurrentMqType().name();
    }
}
```

### 消费者管理

```java
@RestController
@RequiredArgsConstructor
public class ConsumerController {

    private final DynamicMqConsumerManager consumerManager;

    @PostMapping("/consumer/register")
    public String registerConsumer(@RequestBody ConsumerRequest request) {
        ConsumerRegistration registration = ConsumerRegistration.builder()
                .topic(request.getTopic())
                .tag(request.getTag())
                .consumerGroup(request.getGroup())
                .messageHandler(message -> {
                    log.info("动态注册的消费者处理消息: {}", message);
                })
                .build();

        String consumerId = consumerManager.registerConsumer(registration);
        return "消费者注册成功，ID: " + consumerId;
    }

    @PostMapping("/consumer/{consumerId}/stop")
    public String stopConsumer(@PathVariable String consumerId) {
        consumerManager.stopConsumer(consumerId);
        return "消费者已停止";
    }

    @PostMapping("/consumer/{consumerId}/start")
    public String startConsumer(@PathVariable String consumerId) {
        consumerManager.startConsumer(consumerId);
        return "消费者已启动";
    }

    @GetMapping("/consumers")
    public List<ConsumerRegistration> getAllConsumers() {
        return consumerManager.getAllConsumers();
    }
}
    
    public void sendMessage() {
        // 发送简单消息
        mqManager.sendSimple("test-topic", "Hello World!");
        
        // 发送带标签的消息
        mqManager.sendWithTag("test-topic", "tag1", "Hello with tag!");
        
        // 发送延迟消息
        MqMessage delayMessage = MqUtils.buildDelayMessage("test-topic", "Delayed message", 5000L);
        mqManager.send(delayMessage);
        
        // 批量发送
        MqMessage[] messages = {
            MqUtils.buildSimpleMessage("test-topic", "Message 1"),
            MqUtils.buildSimpleMessage("test-topic", "Message 2")
        };
        mqManager.sendBatch(messages);
    }
}
```

#### 消费消息

##### 方式一：使用注解（推荐）

```java
@Slf4j
@DynamicMqListener(
        topic = "test-topic",
        tag = "test-tag",
        group = "test-group",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING,
        consumeThreadMin = 2,
        consumeThreadMax = 10
)
public class AnnotationBasedConsumer implements DynamicMqConsumer {

    @Override
    public void consume(List<MqMessage> messages, Acknowledgement ack) {
        log.info("收到消息，数量: {}", messages.size());

        for (MqMessage message : messages) {
            // 处理消息
            log.info("处理消息: {}", message.getPayload());
        }

        // 确认消息处理成功
        ack.acknowledge();
    }
}
```

##### 方式二：手动注册

```java
@Service
@RequiredArgsConstructor
public class MessageConsumerService {

    private final DynamicMqManager mqManager;

    @PostConstruct
    public void init() {
        // 创建消费者
        DynamicMqConsumer consumer = new DynamicMqConsumer() {
            @Override
            public void consume(List<MqMessage> messages, Acknowledgement ack) {
                for (MqMessage message : messages) {
                    log.info("收到消息: {}", message.getPayload());
                }
                ack.acknowledge();
            }
        };

        // 手动注册消费者
        String consumerId = mqManager.registerConsumer(consumer, "test-topic", "tag1");
        log.info("注册消费者成功: {}", consumerId);
    }
}
```

#### 消费者管理

```java
@Service
@RequiredArgsConstructor
public class ConsumerManagementService {

    private final DynamicMqManager mqManager;

    public void manageConsumers() {
        // 手动注册消费者
        DynamicMqConsumer consumer = (messages, ack) -> {
            // 处理消息逻辑
            ack.acknowledge();
        };
        String consumerId = mqManager.registerConsumer(consumer, "dynamic-topic");

        // 停止消费者
        mqManager.stopConsumer(consumerId);

        // 重新启动消费者
        mqManager.startConsumer(consumerId);

        // 获取所有消费者
        List<ConsumerRegistration> consumers = mqManager.getAllConsumers();

        // 根据主题获取消费者
        List<ConsumerRegistration> topicConsumers = mqManager.getConsumersByTopic("test-topic");

        // 取消注册消费者
        mqManager.unregisterConsumer(consumerId);
    }
}
```

#### 分离式管理（高级用法）

```java
@Service
@RequiredArgsConstructor
public class SeparatedMqService {

    private final DynamicMqProducerManager producerManager;
    private final DynamicMqConsumerManager consumerManager;

    public void demonstrateSeparatedUsage() {
        // 独立使用Producer
        producerManager.sendSimple("topic", "message");

        // 独立使用Consumer
        consumerManager.registerConsumer((messages, ack) -> {
            // 处理消息
            ack.acknowledge();
        }, "topic");

        // 独立切换Producer类型
        producerManager.switchMqType(MqType.REDIS);

        // 独立切换Consumer类型
        consumerManager.switchMqType(MqType.KAFKA);
    }
}
```

#### 动态切换MQ

```java
@Service
@RequiredArgsConstructor
public class MqSwitchService {

    private final DynamicMqManager mqManager;

    public void switchToKafka() {
        // 动态切换到Kafka（同时切换Producer和Consumer）
        mqManager.switchMqType(MqType.KAFKA);
    }

    public void switchToRedis() {
        // 动态切换到Redis
        mqManager.switchMqType(MqType.REDIS);
    }
}
```

## 配置说明

### 基础配置

```yaml
dynamic:
  mq:
    enabled: true                    # 是否启用动态MQ
    type: rocketmq                   # 默认MQ类型
    
    producer:
      send-timeout: 3000             # 发送超时时间（毫秒）
      max-retry-times: 3             # 最大重试次数
      transaction-enabled: false     # 是否启用事务
    
    consumer:
      group-name: default-group      # 消费者组名
      consume-mode: CLUSTERING       # 消费模式
      consume-thread-min: 1          # 最小消费线程数
      consume-thread-max: 10         # 最大消费线程数
      consume-message-batch-max-size: 1  # 批量消费大小
```

### RocketMQ配置

```yaml
dynamic:
  mq:
    rocketmq:
      name-server: localhost:9876
      producer-group: default-producer-group
      properties:
        maxMessageSize: 4194304
```

### RabbitMQ配置

```yaml
dynamic:
  mq:
    rabbitmq:
      host: localhost
      port: 5672
      username: guest
      password: guest
      virtual-host: /
      properties:
        connection-timeout: 30000
        requested-heartbeat: 60
```

### Kafka配置

```yaml
dynamic:
  mq:
    kafka:
      bootstrap-servers: localhost:9092
      properties:
        acks: all
        retries: 3
        batch.size: 16384
```

### Redis配置

```yaml
dynamic:
  mq:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      properties:
        max-active: 20
        max-idle: 10
```

### @DynamicMqListener注解配置

```java
@DynamicMqListener(
    type = "rocketmq",                    // MQ类型，为空时使用默认配置
    topic = "test-topic",                 // 消息主题（必填）
    tag = "test-tag",                     // 消息标签/路由键
    group = "test-group",                 // 消费者组名
    pullBatchSize = 32,                   // 批量拉取消息大小
    consumeMessageBatchMaxSize = 5,       // 批量消费消息数量
    consumeThreadMin = 2,                 // 最小消费线程数
    consumeThreadMax = 10,                // 最大消费线程数
    consumeTimeout = 900000,              // 消费超时时间（毫秒）
    consumeMode = ConsumeMode.CONCURRENTLY, // 消费模式：CONCURRENTLY/ORDERLY
    messageModel = MessageModel.CLUSTERING, // 消息模型：CLUSTERING/BROADCASTING
    selectorType = MessageSelectorType.TAG, // 消息过滤类型：TAG/SQL92
    selectorExpression = "*",             // 消息过滤表达式
    enableMsgTrace = true,                // 是否开启消息轨迹追踪
    autoStartup = true                    // 是否自动启动消费者
)
public class MyConsumer implements DynamicMqConsumer {
    // 实现消费逻辑
}
```

## 架构设计

### 核心组件

1. **MqProducer**: 生产者统一接口
2. **MqConsumer**: 消费者统一接口
3. **AbstractMqProducer**: 生产者抽象类，实现通用逻辑
4. **AbstractMqConsumer**: 消费者抽象类，实现通用逻辑
5. **MqProducerFactory**: 生产者工厂类，创建不同MQ生产者实现
6. **MqConsumerFactory**: 消费者工厂类，创建不同MQ消费者实现
7. **DynamicMqProducerManager**: 生产者管理器，专门管理Producer
8. **DynamicMqConsumerManager**: 消费者管理器，专门管理Consumer
9. **DynamicMqManager**: 统一管理器，提供门面接口
10. **ConsumerRegistry**: 消费者注册中心，管理消费者生命周期

### 设计模式

- **策略模式**: 不同MQ实现相同接口
- **模板方法模式**: 定义消息处理通用流程
- **工厂模式**: 根据配置创建MQ实例
- **单例模式**: 确保MQ实例唯一性
- **分离关注点**: Producer和Consumer完全独立，可单独配置和使用

## 扩展开发

### 添加新的MQ支持

1. 实现`AbstractMqTemplate`抽象类
2. 在`MqTemplateFactory`中添加创建逻辑
3. 在`MqType`枚举中添加新类型
4. 在配置类中添加相应配置

```java
public class CustomMqTemplate extends AbstractMqTemplate {
    
    @Override
    protected boolean doSend(MqMessage message) {
        // 实现发送逻辑
        return true;
    }
    
    @Override
    protected boolean doBatchSend(MqMessage... messages) {
        // 实现批量发送逻辑
        return true;
    }
    
    // 实现其他抽象方法...
}
```

## 分离式设计优势

### Producer和Consumer独立
- **独立初始化**: Producer和Consumer分别初始化，互不影响
- **独立配置**: 可以为Producer和Consumer配置不同的参数
- **独立生命周期**: Producer和Consumer可以独立启停
- **更好的可测试性**: 可以单独测试Producer或Consumer

### 配置示例

```yaml
# 分离式配置
dynamic:
  mq:
    enabled: true
    type: rocketmq  # 默认类型，同时影响Producer和Consumer

    # Producer专用配置
    producer:
      send-timeout: 3000
      max-retry-times: 3

    # Consumer专用配置
    consumer:
      group-name: my-consumer-group
      consume-thread-min: 2
      consume-thread-max: 10
```

## 注意事项

1. 确保目标MQ服务可用后再进行切换
2. 切换MQ时会停止当前消费者，重新启动新的消费者
3. 不同MQ的特性支持程度不同，请参考支持列表
4. 生产环境建议做好监控和异常处理
5. Producer和Consumer现在完全独立，可以分别配置和管理

## 📊 监控和健康检查

框架集成了Spring Boot Actuator，提供健康检查功能：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

访问健康检查端点：
```bash
curl http://localhost:8080/actuator/health
```

## 🔧 故障排查

### 常见问题

1. **MQ连接失败**
   - 检查MQ服务是否启动
   - 验证连接配置是否正确
   - 检查网络连通性

2. **消息发送失败**
   - 检查Topic是否存在
   - 验证权限配置
   - 查看错误日志

3. **消费者无法接收消息**
   - 检查消费者组配置
   - 验证Topic和Tag匹配
   - 确认消费者是否正常启动

### 日志配置

```yaml
logging:
  level:
    io.github.kk01001.dynamic.mq: DEBUG
```

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📝 更新日志

### v2.4.6.5
- ✅ 修复所有编译错误和警告
- ✅ 更新过时API使用
- ✅ 完善文档和示例
- ✅ 支持Java 21

### v2.4.6.4
- 🚀 支持动态MQ切换
- 🔧 分离Producer和Consumer管理
- 📊 添加健康检查支持

## 📋 版本兼容

| 组件 | 版本要求 |
|------|---------|
| **Java** | 21+ |
| **Spring Boot** | 3.x |
| **Maven** | 3.6+ |
| **RocketMQ** | 5.x |
| **RabbitMQ** | 3.8+ |
| **Kafka** | 2.8+ |
| **Redis** | 6.0+ |

## 📄 许可证

```
Copyright 2024 kk01001

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！

---

**如果这个项目对你有帮助，请给个 ⭐ Star 支持一下！**
