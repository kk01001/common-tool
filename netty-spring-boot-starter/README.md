# Netty WebSocket Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/netty-spring-boot-starter.svg)](https://search.maven.org/artifact/io.github.archer099/netty-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

## 简介

`netty-spring-boot-starter` 是一个基于 Netty 的 WebSocket 服务器的 Spring Boot 启动器，提供了简单易用的 WebSocket 开发体验。通过注解驱动的方式，帮助开发者快速搭建高性能的 WebSocket 服务。

## 功能特点

- 🚀 **高性能**：基于 Netty 构建，提供卓越的性能和可扩展性
- 🔌 **注解驱动**：通过简单的注解即可定义 WebSocket 端点和处理方法
- 🔒 **安全支持**：内置认证机制和 SSL/TLS 支持
- 💻 **会话管理**：完善的会话管理机制，支持一对一通信和广播消息
- 🌐 **集群支持**：基于 Redis 的集群方案，实现分布式 WebSocket 服务
- 💓 **心跳检测**：自动处理连接心跳，确保连接稳定性
- 📊 **监控指标**：集成 Micrometer，提供运行时监控指标

## 技术栈

- Java 21
- Spring Boot 3.x
- Netty 4.x
- Redis (用于集群支持)
- Jackson (消息序列化)

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>netty-spring-boot-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

### 配置属性

在 `application.properties` 或 `application.yml` 中配置：

```yaml
netty:
  websocket:
    # 是否启用 WebSocket 服务器
    enabled: true
    # WebSocket 服务器端口
    port: 8081
    # WebSocket 路径
    path: /ws
    # 工作线程数（默认为 CPU 核心数 * 2）
    worker-threads: 4
    # 最大帧大小（字节）
    max-frame-size: 65536
    # 会话超时时间（分钟）
    session-timeout: 30
    
    # 心跳配置
    heartbeat:
      enabled: true
      reader-idle-time: 60
      writer-idle-time: 30
    
    # 认证配置
    auth-enabled: false
    
    # SSL 配置
    ssl:
      enabled: false
      cert-path: /path/to/cert.pem
      key-path: /path/to/key.pem
      key-password: password
    
    # 集群配置
    cluster:
      enabled: false
      session-timeout: 60m
      cleanup-interval: 30m
      session-key-prefix: netty-ws:session:
      node-key-prefix: netty-ws:node
      broadcast-channel-prefix: netty-ws:broadcast:
      session-shard-count: 5
```

### 创建 WebSocket 端点

使用 `@WebSocketEndpoint` 注解来定义 WebSocket 端点：

```java
import io.github.archer099.netty.annotation.OnBinaryMessage;
import io.github.archer099.netty.annotation.OnClose;
import io.github.archer099.netty.annotation.OnError;
import io.github.archer099.netty.annotation.OnMessage;
import io.github.archer099.netty.annotation.OnOpen;
import io.github.archer099.netty.annotation.WebSocketEndpoint;
import io.github.archer099.netty.session.WebSocketSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@WebSocketEndpoint
public class MyWebSocketHandler {

    @OnOpen
    public void onOpen(WebSocketSession session) {
        log.info("新的连接建立: {}", session.getId());
    }

    @OnMessage
    public void onMessage(WebSocketSession session, String message) {
        log.info("收到消息: {}", message);
        // 回复消息
        session.sendMessage("收到你的消息: " + message);
    }

    @OnBinaryMessage
    public void onBinaryMessage(WebSocketSession session, byte[] data) {
        log.info("收到二进制消息: {} 字节", data.length);
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        log.info("连接关闭: {}", session.getId());
    }

    @OnError
    public void onError(WebSocketSession session, Throwable error) {
        log.error("连接发生错误: {}", session.getId(), error);
    }
}
```

## 会话管理

### WebSocketSession

`WebSocketSession` 提供了丰富的方法来管理连接和发送消息：

```java
// 发送消息给当前会话
session.sendMessage("Hello");

// 广播消息给同路径的其他会话（不包括自己）
session.broadcast("广播消息");

// 广播消息给所有会话（包括自己）
session.broadcastAll("广播给所有人");

// 发送消息给特定会话
session.sendToSession("目标会话ID", "定向消息");

// 关闭会话
session.close();

// 设置和获取会话属性
session.setAttribute("key", value);
Object value = session.getAttribute("key");

// 获取会话ID
String sessionId = session.getId();

// 获取用户ID（如果已认证）
String userId = session.getUserId();
```

### WebSocketSessionManager

`WebSocketSessionManager` 负责全局会话管理：

```java
@Autowired
private WebSocketSessionManager sessionManager;

// 获取特定路径下的所有会话
Set<WebSocketSession> sessions = sessionManager.getSessions("/ws");

// 广播消息给特定路径下的所有会话
sessionManager.broadcast("/ws", "全局广播消息");

// 广播消息给符合条件的会话
sessionManager.broadcast("/ws", "筛选广播消息", 
    session -> "VIP".equals(session.getAttribute("userType")));

// 发送消息给指定会话
sessionManager.sendToSession("/ws", "目标会话ID", "管理员消息");

// 获取会话数量
int count = sessionManager.getSessionCount("/ws");
```

## 认证与安全

### 启用认证

配置 `netty.websocket.auth-enabled=true` 启用认证，然后实现 `WebSocketAuthenticator` 接口：

```java
import io.github.archer099.netty.auth.WebSocketAuthenticator;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.springframework.stereotype.Component;

@Component
public class TokenAuthenticator implements WebSocketAuthenticator {

    @Override
    public String authenticate(FullHttpRequest request) {
        // 从请求头或查询参数中获取令牌
        String token = request.headers().get(HttpHeaderNames.AUTHORIZATION);
        if (token == null) {
            // 尝试从查询参数获取
            String uri = request.uri();
            if (uri.contains("token=")) {
                token = uri.substring(uri.indexOf("token=") + 6);
                if (token.contains("&")) {
                    token = token.substring(0, token.indexOf("&"));
                }
            }
        }
        
        // 验证令牌
        if (isValidToken(token)) {
            // 返回用户ID
            return extractUserIdFromToken(token);
        }
        
        // 返回null表示认证失败
        return null;
    }
    
    private boolean isValidToken(String token) {
        // 实现令牌验证逻辑
        return token != null && !token.isEmpty();
    }
    
    private String extractUserIdFromToken(String token) {
        // 从令牌中提取用户ID
        return "user-" + token.hashCode();
    }
}
```

### 启用 SSL/TLS

配置 SSL 相关属性即可启用 HTTPS WebSocket (wss://):

```yaml
netty:
  websocket:
    ssl:
      enabled: true
      cert-path: /path/to/cert.pem
      key-path: /path/to/key.pem
      key-password: password
```

## 集群支持

### 启用集群模式

配置 Redis 和集群属性：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    
netty:
  websocket:
    cluster:
      enabled: true
```

集群模式下，WebSocket 消息会自动在多个节点间同步，支持：

- 跨节点会话管理
- 跨节点消息广播
- 节点状态监控与自动故障处理

### 自定义消息处理

实现 `ClusterMessageHandler` 接口可自定义集群消息处理逻辑：

```java
import io.github.archer099.netty.cluster.ClusterMessageHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomClusterMessageHandler implements ClusterMessageHandler {

    @Override
    public void handleNodeEvent(String nodeId, NodeEvent event) {
        // 处理节点事件（上线、下线、超时）
    }

    @Override
    public void handleBroadcastMessage(String sourceNodeId, String path, String message, String targetSessionId) {
        // 处理从其他节点广播的消息
    }
}
```

## 监控与指标

启动器集成了 Micrometer 提供监控指标，可与 Prometheus、Datadog 等监控系统整合：

- 连接数指标: `websocket.connections`
- 消息计数: `websocket.messages.sent`, `websocket.messages.received`
- 消息延迟: `websocket.message.latency`
- 错误计数: `websocket.errors`

## 消息过滤器

实现 `MessageFilter` 接口可以过滤和转换消息：

```java
import io.github.archer099.netty.filter.MessageFilter;
import io.github.archer099.netty.session.WebSocketSession;
import org.springframework.stereotype.Component;

@Component
public class SensitiveWordFilter implements MessageFilter {

    @Override
    public boolean doFilter(WebSocketSession session, String message) {
        // 检查并过滤敏感词
        if (containsSensitiveWords(message)) {
            session.sendMessage("消息包含敏感词，已被过滤");
            return false; // 返回false阻止消息继续传递
        }
        return true; // 返回true允许消息继续传递
    }

    @Override
    public int getOrder() {
        return 1; // 过滤器执行顺序
    }
    
    private boolean containsSensitiveWords(String message) {
        // 实现敏感词检查逻辑
        return false;
    }
}
```

## 自定义通道配置

实现 `WebSocketPipelineConfigurer` 接口可以自定义 Netty 管道：

```java
import io.github.archer099.netty.config.WebSocketPipelineConfigurer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.stereotype.Component;

@Component
public class LoggingPipelineConfigurer implements WebSocketPipelineConfigurer {

    @Override
    public void configure(ChannelPipeline pipeline) {
        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
}
```

## 客户端连接示例

### JavaScript 客户端

```javascript
// 建立连接
const socket = new WebSocket('ws://localhost:8081/ws');

// 连接建立时
socket.onopen = function(event) {
  console.log('连接已建立');
  socket.send('Hello Server!');
};

// 接收消息
socket.onmessage = function(event) {
  console.log('收到消息: ' + event.data);
};

// 连接关闭
socket.onclose = function(event) {
  console.log('连接已关闭');
};

// 发生错误
socket.onerror = function(error) {
  console.error('WebSocket 错误:', error);
};

// 发送消息
function sendMessage(message) {
  socket.send(message);
}

// 关闭连接
function closeConnection() {
  socket.close();
}
```

### 带认证的连接

```javascript
// 带令牌的连接
const socket = new WebSocket('ws://localhost:8081/ws?token=YOUR_AUTH_TOKEN');

// 或者通过自定义请求头（可能需要服务器支持）
const socket = new WebSocket('ws://localhost:8081/ws');
socket.setRequestHeader('Authorization', 'Bearer YOUR_AUTH_TOKEN');
```

## 最佳实践

### 性能优化

1. **调整工作线程数**：对于 I/O 密集型应用，建议设置为 CPU 核心数的 2 倍
2. **优化消息大小**：避免发送过大的消息，考虑分片或压缩
3. **使用二进制消息**：对于大量数据传输，使用二进制消息而非文本消息

### 高可用性

1. **启用集群模式**：在生产环境中启用集群支持，实现跨节点消息同步
2. **配置合理的超时时间**：根据业务场景配置合适的连接和会话超时时间
3. **实现优雅关闭**：处理服务关闭时的连接断开，通知客户端重连

### 安全建议

1. **始终启用认证**：生产环境应始终启用认证机制
2. **使用 SSL/TLS**：生产环境建议启用 SSL 加密传输
3. **实现消息验证**：验证消息格式和内容，防止恶意请求
4. **限制连接数**：针对每个用户或 IP 限制最大连接数，防止资源耗尽

## 应用场景

- **实时通知系统**：推送即时消息和通知
- **在线聊天应用**：一对一或群组聊天
- **实时协作工具**：文档协作编辑、白板应用
- **实时监控系统**：设备状态监控和报警
- **在线游戏**：多人互动游戏、游戏状态同步
- **金融交易平台**：实时报价和交易通知

## 常见问题

### Q: 如何处理大量连接?
A: 调整工作线程数、优化 JVM 参数，考虑水平扩展集群

### Q: 客户端无法连接怎么办?
A: 检查端口配置、防火墙设置，确认服务已正常启动

### Q: 消息延迟高怎么解决?
A: 检查网络环境、优化消息大小和格式、增加服务器资源

### Q: 认证失败怎么处理?
A: 检查认证实现逻辑、验证令牌是否正确传递

## 许可证

本项目遵循 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源许可证。

## 贡献指南

欢迎提交问题和贡献代码，请通过 GitHub Issue 和 Pull Request 参与项目开发。 