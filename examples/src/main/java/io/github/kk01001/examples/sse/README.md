# SSE Spring Boot Starter 示例

本示例演示了如何使用 SSE Spring Boot Starter 来实现服务器推送事件功能。

## 功能演示

1. SSE连接建立与断开
2. 向特定用户发送消息
3. 向特定主题发送消息
4. 广播消息给所有客户端
5. 自定义消息发送

## 如何运行

1. 启动应用程序
2. 访问 `http://localhost:8080/sse-test.html` 打开测试页面

## 测试步骤

### 建立连接

1. 在"连接管理"区域，可以选择填写客户端ID、用户ID和主题（都是可选的）
2. 点击"连接"按钮建立SSE连接
3. 连接成功后，状态会显示为"已连接"

### 发送消息

1. 在"发送消息"区域，选择消息类型：
   - 发送给用户：需要填写目标用户ID
   - 发送给主题：需要填写目标主题
   - 广播消息：发送给所有连接的客户端
2. 输入消息内容
3. 点击"发送消息"按钮

### 接收消息

所有接收到的消息会显示在"接收消息"区域，不同类型的消息会有不同的背景色：
- 用户消息：蓝色背景
- 主题消息：绿色背景
- 广播消息：黄色背景
- 连接消息：灰色背景

## API说明

### 后端接口

| 接口 | 方法 | 描述 |
| --- | --- | --- |
| `/example/sse/connect` | GET | 创建SSE连接 |
| `/example/sse/send/user/{userId}` | POST | 向指定用户发送消息 |
| `/example/sse/send/topic/{topic}` | POST | 向指定主题发送消息 |
| `/example/sse/broadcast` | POST | 广播消息 |
| `/example/sse/send/custom` | POST | 发送自定义消息 |
| `/example/sse/count` | GET | 获取当前连接数 |

### 前端使用

```javascript
// 创建SSE连接
const eventSource = new EventSource('/example/sse/connect?clientId=client123&userId=user456&topic=notifications');

// 监听连接成功事件
eventSource.addEventListener('connect', function(event) {
  console.log('连接成功:', event.data);
});

// 监听自定义事件
eventSource.addEventListener('custom-event', function(event) {
  console.log('收到自定义事件消息:', event.data);
});

// 关闭连接
eventSource.close();
``` 