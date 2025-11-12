# MQTT Web 测试说明

## HTML Demo

- **位置**：`src/test/resources/static/mqtt-demo.html`
- **功能**：提供一个基于 MQTT over WebSocket 的页面，用于手动测试消息发送和接收。

### 使用步骤

1. 确保 MQTT Broker 支持 WebSocket，例如 EMQX 默认 8083 端口。
2. 在浏览器中打开 `mqtt-demo.html` 文件。
3. 填写 WebSocket 地址，例如：`ws://localhost:8083/mqtt`
4. 点击「连接」，成功后可以：
   - 在「订阅配置」中填写主题并订阅。
   - 在「消息发送」区域输入主题和内容并发送。
   - 收到的消息会显示在页面下方的日志区域，可点击「回复收到的最新消息」。

## Spring Boot 测试程序

- **类路径**：`src/test/java/io/github/kk01001/mqtt/MqttTestApplication.java`
- **作用**：启动后每 100ms 向 `demo/topic` 发布一条 JSON 消息，共发送 100 条。

### 运行步骤

1. 确保 `application.yml` 中配置的 MQTT Broker 可用。
2. 运行 `MqttTestApplication`。
3. 观察日志，可看到消息序列号和时间戳。
4. 在 HTML Demo 中订阅相同主题 `demo/topic`，可以实时看到消息列表。

## 建议组合测试

- 启动 Broker（如 EMQX 或 Mosquitto）。
- 运行 `MqttTestApplication` 发送测试数据。
- 在浏览器中打开 `mqtt-demo.html`，订阅 `demo/topic`，验证消息收发与回复。
