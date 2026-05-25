# MQTT Spring Boot Starter

基于 Eclipse Paho MQTT 客户端实现的 Spring Boot Starter，支持 EMQX 等 MQTT Broker，提供简单易用的消息发送和接收功能。

## 特性

### 核心功能

- ✅ **开箱即用**：自动配置 MQTT 客户端，无需手动管理连接
- ✅ **注解驱动**：使用 `@MqttMessageListener` 注解轻松接收消息
- ✅ **模板发送**：提供 `MqttTemplate` 模板类，支持同步和异步发送
- ✅ **灵活配置**：支持丰富的配置选项，满足各种场景需求
- ✅ **自动重连**：支持自动重连和断线重连后自动重新订阅
- ✅ **线程池管理**：内置消费者线程池，高效处理消息
- ✅ **通配符订阅**：支持 MQTT 通配符（`+` 和 `#`）订阅
- ✅ **QoS 支持**：支持 QoS 0、1、2 三种消息质量等级
- ✅ **遗嘱消息**：支持配置遗嘱消息（Last Will and Testament）

### 高级功能

- ✅ **广播和批量发送**：支持向多个设备同时发送消息，适用于 IoT 设备管理
- ✅ **SSL/TLS 加密**：支持 SSL/TLS 加密连接和双向认证
- ✅ **健康检查**：集成 Spring Boot Actuator 健康检查
- ✅ **消息拦截器**：支持自定义消息拦截器，用于日志、监控等
- ✅ **指标监控**：内置消息收发指标统计
- ✅ **异常处理**：完善的异常体系和处理机制

## 快速开始

### 1. 添加依赖

在 `pom.xml` 中添加依赖：

```xml

<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>mqtt-spring-boot-starter</artifactId>
    <version>latest</version>
</dependency>
```

### 2. 配置 MQTT

在 `application.yml` 中添加配置：

```yaml
mqtt:
  enabled: true
  broker-url: tcp://localhost:1883
  client-id: mqtt-client-demo
  username: your-username
  password: your-password
  clean-session: true
  automatic-reconnect: true
```

### 3. 发送消息

注入 `MqttTemplate` 并发送消息：

```java

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MqttTemplate mqttTemplate;

    public void sendMessage() throws MqttException {
        // 发送简单消息
        mqttTemplate.send("testtopic/1", "Hello MQTT!");

        // 指定 QoS
        mqttTemplate.send("testtopic/2", "Hello with QoS 2", 2);

        // 发送保留消息
        mqttTemplate.send("testtopic/3", "Retained message", 1, true);

        // 异步发送
        mqttTemplate.sendAsync("testtopic/4", "Async message")
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("发送失败", ex);
                    } else {
                        log.info("发送成功");
                    }
                });
    }
}
```

### 4. 接收消息

使用 `@MqttMessageListener` 注解接收消息：

```java

@Component
@Slf4j
public class MessageListener {

    // 接收单个主题的消息
    @MqttMessageListener(topics = "testtopic/1", qos = 1)
    public void handleMessage(String payload) {
        log.info("收到消息: {}", payload);
    }

    // 接收多个主题的消息
    @MqttMessageListener(topics = {"testtopic/2", "testtopic/3"}, qos = 1)
    public void handleMultipleTopics(String topic, String payload) {
        log.info("收到消息 - 主题: {}, 内容: {}", topic, payload);
    }

    // 使用通配符订阅
    @MqttMessageListener(topics = "testtopic/#", qos = 2)
    public void handleWildcard(String topic, MqttMessage message) {
        log.info("收到消息 - 主题: {}, QoS: {}, 内容: {}",
                topic, message.getQos(), new String(message.getPayload()));
    }

    // 接收字节数组
    @MqttMessageListener(topics = "testtopic/binary", qos = 1)
    public void handleBinary(String topic, byte[] payload) {
        log.info("收到二进制消息 - 主题: {}, 长度: {} bytes", topic, payload.length);
    }
}
```

## 配置说明

### 基础配置

| 配置项                        | 类型       | 默认值   | 说明                                     |
|----------------------------|----------|-------|----------------------------------------|
| `mqtt.enabled`             | Boolean  | false | 是否启用 MQTT                              |
| `mqtt.broker-url`          | String   | -     | MQTT Broker 地址，例如：tcp://localhost:1883 |
| `mqtt.client-id`           | String   | 自动生成  | 客户端 ID                                 |
| `mqtt.username`            | String   | -     | 用户名                                    |
| `mqtt.password`            | String   | -     | 密码                                     |
| `mqtt.clean-session`       | Boolean  | true  | 是否清除会话                                 |
| `mqtt.connection-timeout`  | Duration | 30s   | 连接超时时间                                 |
| `mqtt.keep-alive-interval` | Duration | 60s   | 保持连接时间间隔                               |
| `mqtt.automatic-reconnect` | Boolean  | true  | 是否自动重连                                 |
| `mqtt.max-reconnect-delay` | Duration | 128s  | 最大重连延迟时间                               |

### 遗嘱消息配置

| 配置项                  | 类型      | 默认值   | 说明       |
|----------------------|---------|-------|----------|
| `mqtt.will.topic`    | String  | -     | 遗嘱主题     |
| `mqtt.will.payload`  | String  | -     | 遗嘱消息内容   |
| `mqtt.will.qos`      | Integer | 1     | 遗嘱消息 QoS |
| `mqtt.will.retained` | Boolean | false | 是否保留遗嘱消息 |

### 生产者配置

| 配置项                              | 类型       | 默认值   | 说明       |
|----------------------------------|----------|-------|----------|
| `mqtt.producer.default-qos`      | Integer  | 1     | 默认 QoS   |
| `mqtt.producer.default-retained` | Boolean  | false | 默认是否保留消息 |
| `mqtt.producer.send-timeout`     | Duration | 5s    | 发送超时时间   |
| `mqtt.producer.async`            | Boolean  | false | 是否异步发送   |

### 消费者配置

| 配置项                              | 类型       | 默认值  | 说明        |
|----------------------------------|----------|------|-----------|
| `mqtt.consumer.topics`           | String[] | -    | 默认订阅的主题   |
| `mqtt.consumer.default-qos`      | Integer  | 1    | 默认 QoS    |
| `mqtt.consumer.thread-pool-size` | Integer  | 10   | 消费线程池大小   |
| `mqtt.consumer.queue-capacity`   | Integer  | 1000 | 消费线程池队列大小 |

## 完整配置示例

```yaml
mqtt:
  enabled: true
  broker-url: tcp://10.255.1.21:1883
  client-id: mqtt-client-demo
  username: emqx_test
  password: emqx_test_password
  clean-session: true
  connection-timeout: 30s
  keep-alive-interval: 60s
  automatic-reconnect: true
  max-reconnect-delay: 128s

  will:
    topic: testtopic/will
    payload: Client disconnected unexpectedly
    qos: 1
    retained: false

  producer:
    default-qos: 1
    default-retained: false
    send-timeout: 5s
    async: false

  consumer:
    topics:
      - testtopic/#
    default-qos: 1
    thread-pool-size: 10
    queue-capacity: 1000
```

## MQTT 通配符说明

MQTT 支持两种通配符：

- **单层通配符 `+`**：匹配一个层级
    - 例如：`testtopic/+/status` 可以匹配 `testtopic/device1/status` 和 `testtopic/device2/status`

- **多层通配符 `#`**：匹配多个层级
    - 例如：`testtopic/#` 可以匹配 `testtopic/1`、`testtopic/1/2`、`testtopic/1/2/3` 等

## QoS 说明

MQTT 支持三种 QoS 级别：

- **QoS 0**：最多一次（At most once）
    - 消息发送后不管是否到达，不会重发
    - 性能最高，但可能丢失消息

- **QoS 1**：至少一次（At least once）
    - 保证消息至少到达一次，可能重复
    - 平衡性能和可靠性

- **QoS 2**：只有一次（Exactly once）
    - 保证消息只到达一次，不会重复
    - 可靠性最高，但性能较低

## 监听器方法参数说明

`@MqttMessageListener` 注解的方法支持以下参数类型：

| 参数类型                  | 说明                            |
|-----------------------|-------------------------------|
| `String`              | 第一个参数为主题，第二个参数为消息内容（字符串）      |
| `byte[]`              | 消息内容（字节数组）                    |
| `MqttMessage`         | MQTT 消息对象，包含 QoS、retained 等信息 |
| `int` / `Integer`     | 消息的 QoS 级别                    |
| `boolean` / `Boolean` | 消息是否为保留消息                     |

示例：

```java
// 只接收消息内容
@MqttMessageListener(topics = "topic1", qos = 1)
public void handle1(String payload) {
}

// 接收主题和消息内容
@MqttMessageListener(topics = "topic2", qos = 1)
public void handle2(String topic, String payload) {
}

// 接收 MqttMessage 对象
@MqttMessageListener(topics = "topic3", qos = 1)
public void handle3(String topic, MqttMessage message) {
}

// 接收字节数组
@MqttMessageListener(topics = "topic4", qos = 1)
public void handle4(String topic, byte[] payload) {
}
```

## 高级用法

### 动态订阅和取消订阅

```java

@Service
@RequiredArgsConstructor
public class DynamicSubscriptionService {

    private final MqttTemplate mqttTemplate;

    // 动态订阅
    public void subscribe(String topic) throws MqttException {
        mqttTemplate.subscribe(topic, 1);
    }

    // 取消订阅
    public void unsubscribe(String topic) throws MqttException {
        mqttTemplate.unsubscribe(topic);
    }
}
```

### 自定义 MQTT 客户端管理器

```java

@Configuration
public class CustomMqttConfig {

    @Bean
    public MqttClientManager customMqttClientManager(MqttProperties properties) throws MqttException {
        MqttClientManager manager = new MqttClientManager(properties);
        // 自定义配置
        manager.connect();
        return manager;
    }
}
```

### 共享订阅（负载均衡消费）

共享订阅用于在多个订阅者之间做负载均衡。同一条消息会投递到所有“组”，但每个组内仅一个客户端接收。适合后端处理集群，不适合前端广播。

前缀与格式：

- 带组共享订阅：`$share/<group>/<original-topic>` 示例：`$share/danmaku/video/+/danmaku`
- 无组共享订阅（特例）：`$queue/<original-topic>` 示例：`$queue/video/123/danmaku`

代码示例：

```java
@Component
public class SharedListener {
    @MqttMessageListener(topics = "$share/metrics/app/+/event", qos = 1)
    public void handle(String topic, String payload) {
        // 该组内多实例均衡接收
    }
}
```

最佳实践：

- 前端广播订阅原始主题；后端消费用 `$share/<group>/...`
- 使用 `clean_session=true` 或 MQTT v5 短会话过期，避免断线期间队列积压
- 不在共享订阅上使用保留消息，避免历史消息误分配

### 延迟发布（EMQX 扩展）

EMQX 支持通过特殊主题前缀延迟发布：`$delayed/{DelayInterval}/{TopicName}`。例如：`$delayed/10/demo/delay` 表示 10 秒后发布到 `demo/delay`。

Starter 已封装延迟发布方法：

```java
@Service
@RequiredArgsConstructor
public class DelaySender {
    private final MqttTemplate mqttTemplate;

    public void send(String msg) throws MqttException {
        // 同步延迟发布
        mqttTemplate.sendDelayed("demo/delay", msg, 10);

        // 异步延迟发布
        mqttTemplate.sendDelayedAsync("demo/delay", msg, 5)
            .whenComplete((v, ex) -> { /* 结果处理 */ });
    }
}
```

注意：

- `delaySeconds` 范围为 `1..4294967`
- 延迟发布为 EMQX 企业版功能，需在 Dashboard 启用“延迟发布”

## 注意事项

1. **客户端 ID**：如果不指定 `client-id`，系统会自动生成一个唯一的客户端 ID
2. **Clean Session**：设置为 `true` 时，断开连接后会清除会话状态；设置为 `false` 时，会保留会话状态
3. **自动重连**：启用自动重连后，连接断开时会自动尝试重连，并重新订阅所有主题
4. **线程池**：消息消费使用独立的线程池，避免阻塞 MQTT 客户端的回调线程
5. **QoS 选择**：根据业务需求选择合适的 QoS 级别，平衡性能和可靠性
6. **共享订阅与广播**：前端广播请使用原始主题订阅；共享订阅用于后端负载均衡
7. **延迟发布**：使用 `$delayed/秒数/原始主题` 语义或封装方法；评估业务对延迟语义的依赖

## 兼容性

- Spring Boot 2.x / 3.x
- JDK 8+
- Eclipse Paho MQTT Client 1.2.5
- 支持 EMQX、Mosquitto 等标准 MQTT Broker

## 文档

- [快速开始指南](QUICKSTART.md) - 5 分钟快速上手
- [使用指南](USAGE.md) - 详细的使用说明和工作原理
- [高级功能](ADVANCED.md) - SSL/TLS、拦截器、监控等高级功能
- [共享订阅](https://docs.emqx.com/zh/emqx/v5.8/messaging/mqtt-shared-subscription.html)
- [延迟发布](https://docs.emqx.com/zh/emqx/v5.8/messaging/mqtt-delayed-publish.html)

## 参考文档

- [Eclipse Paho MQTT Client](https://eclipse.dev/paho/files/javadoc/index.html)
- [MQTT 协议规范](https://mqtt.org/)
- [EMQX 文档](https://www.emqx.io/docs/)

## License

Apache License 2.0
