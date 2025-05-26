# SSE Spring Boot Starter

一个用于快速集成 Server-Sent Events (SSE) 的 Spring Boot Starter，支持集群环境，提供多种消息转发方式。

## 特性

- 快速集成 SSE 到 Spring Boot 应用
- 支持集群环境下的消息同步
- 支持 Redis 和 RocketMQ 作为消息转发中间件
- 支持多种消息发送模式：点对点、用户广播、主题订阅、全局广播
- 支持心跳机制，保持连接活跃
- 提供完整的 API 接口和控制器

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>sse-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>

<!-- 如果使用 Redis 作为消息转发 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- 如果使用 RocketMQ 作为消息转发 -->
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>2.2.3</version>
</dependency>
```

### 2. 配置属性

```yaml
sse:
  enabled: true                   # 是否启用 SSE，默认为 true
  cluster: true                   # 是否启用集群模式，默认为 false
  message-forward-type: redis     # 消息转发方式：redis, rocketmq, none(默认)
  heartbeat-interval: 10000       # 心跳间隔（毫秒），默认 10000
  client-timeout: 60000           # 客户端超时时间（毫秒），默认 60000
  controller:
    enabled: true                 # 是否启用内置控制器，默认为 true
  
  # Redis 配置（当 message-forward-type 为 redis 时有效）
  redis:
    topic: sse:message            # Redis 发布订阅主题，默认为 sse:message
  
  # RocketMQ 配置（当 message-forward-type 为 rocketmq 时有效）
  rocketMq:
    topic: sse-message            # RocketMQ 主题，默认为 sse-message
    consumer-group: sse-consumer-group  # 消费者组，默认为 sse-consumer-group
    consumer-count: 1             # 消费者线程数，默认为 1
```

### 3. 使用示例

#### 3.1 客户端连接 SSE

```javascript
// 前端 JavaScript
const eventSource = new EventSource('/sse/connect?clientId=client123&userId=user456&topic=notifications');

// 监听连接成功事件
eventSource.addEventListener('connect', function(event) {
  console.log('SSE 连接成功:', event.data);
});

// 监听自定义事件
eventSource.addEventListener('custom-event', function(event) {
  console.log('收到自定义事件消息:', event.data);
});

// 监听默认消息
eventSource.onmessage = function(event) {
  console.log('收到默认消息:', event.data);
};

// 关闭连接
function closeConnection() {
  eventSource.close();
  // 可选：通知服务端关闭连接
  fetch('/sse/disconnect?clientId=client123');
}
```

#### 3.2 服务端发送消息

##### 使用内置控制器

```java
// 发送消息到指定客户端
@PostMapping("/send-to-client")
public String sendToClient() {
    SseMessage message = SseMessage.builder()
            .id(UUID.randomUUID().toString())
            .event("custom-event")
            .data("这是发送给特定客户端的消息")
            .build();
    
    // 发送 HTTP 请求到内置控制器
    restTemplate.postForEntity("/sse/send/client/client123", message, String.class);
    return "消息已发送";
}
```

##### 使用 SseService

```java
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final SseService sseService;
    
    public void sendNotification(String userId, String message) {
        // 发送消息给指定用户
        sseService.sendMessageToUser(userId, message, "notification");
    }
    
    public void broadcastAnnouncement(String message) {
        // 广播消息给所有连接的客户端
        sseService.broadcastMessage(message, "announcement");
    }
    
    public void sendTopicMessage(String topic, String message) {
        // 发送消息给订阅特定主题的客户端
        sseService.sendMessageToTopic(topic, message, "topic-message");
    }
}
```

## API 参考

### SseService 接口

```java
// 创建 SSE 连接
SseEmitter createConnection(String clientId, String userId, String topic);

// 关闭 SSE 连接
void closeConnection(String clientId);

// 发送消息
void sendMessage(SseMessage message);

// 向指定客户端发送消息
void sendMessageToClient(String clientId, SseMessage message);
void sendMessageToClient(String clientId, String data, String event);

// 向指定用户发送消息
void sendMessageToUser(String userId, SseMessage message);
void sendMessageToUser(String userId, String data, String event);

// 向指定主题发送消息
void sendMessageToTopic(String topic, SseMessage message);
void sendMessageToTopic(String topic, String data, String event);

// 广播消息
void broadcastMessage(SseMessage message);
void broadcastMessage(String data, String event);

// 获取连接数量
int getConnectionCount();
```

### 内置 REST API

| 路径 | 方法 | 描述 |
| --- | --- | --- |
| `/sse/connect` | GET | 创建 SSE 连接 |
| `/sse/disconnect` | GET | 关闭 SSE 连接 |
| `/sse/send` | POST | 发送消息 |
| `/sse/send/client/{clientId}` | POST | 向指定客户端发送消息 |
| `/sse/send/user/{userId}` | POST | 向指定用户发送消息 |
| `/sse/send/topic/{topic}` | POST | 向指定主题发送消息 |
| `/sse/broadcast` | POST | 广播消息 |
| `/sse/count` | GET | 获取连接数量 |

## 集群支持

在集群环境中，需要配置消息转发方式以确保消息能够在不同节点间同步：

### Redis 模式

```yaml
sse:
  cluster: true
  message-forward-type: redis
  redis:
    topic: sse:message
```

### RocketMQ 模式

```yaml
sse:
  cluster: true
  message-forward-type: rocketmq
  rocketMq:
    topic: sse-message
    consumer-group: sse-consumer-group
```

## 注意事项

1. 在生产环境中，建议配置合适的心跳间隔和客户端超时时间
2. 使用集群模式时，确保消息中间件（Redis/RocketMQ）正确配置并可用
3. 对于高并发场景，可能需要调整 Web 服务器的连接超时和线程池配置
4. 某些代理服务器可能不支持 SSE，请确保网络环境支持长连接 