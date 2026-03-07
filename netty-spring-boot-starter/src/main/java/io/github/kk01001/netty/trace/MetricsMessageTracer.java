package io.github.kk01001.netty.trace;

import io.github.kk01001.netty.config.NettyWebSocketProperties;
import io.github.kk01001.netty.session.WebSocketSession;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kk01001
 * @date 2026-03-07 10:00:00
 * @description 基于 Micrometer 的消息追踪器，Counter/Timer 在构造时一次性创建
 */
@Slf4j
public class MetricsMessageTracer implements MessageTracer {

    private final Counter sentCounter;
    private final Counter receivedCounter;
    private final Counter errorCounter;
    private final Counter connectCounter;
    private final Counter disconnectCounter;
    private final Timer messageLatencyTimer;

    public MetricsMessageTracer(MeterRegistry registry, NettyWebSocketProperties properties) {
        String path = properties.getPath();
        String port = String.valueOf(properties.getPort());

        this.sentCounter = Counter.builder("websocket.messages.sent")
                .description("WebSocket发送消息计数")
                .tags("path", path, "port", port)
                .register(registry);

        this.receivedCounter = Counter.builder("websocket.messages.received")
                .description("WebSocket接收消息计数")
                .tags("path", path, "port", port)
                .register(registry);

        this.errorCounter = Counter.builder("websocket.errors")
                .description("WebSocket错误计数")
                .tags("path", path, "port", port)
                .register(registry);

        this.connectCounter = Counter.builder("websocket.connections")
                .description("WebSocket连接计数")
                .tags("path", path, "port", port)
                .register(registry);

        this.disconnectCounter = Counter.builder("websocket.disconnections")
                .description("WebSocket断开连接计数")
                .tags("path", path, "port", port)
                .register(registry);

        this.messageLatencyTimer = Timer.builder("websocket.message.latency")
                .description("WebSocket消息处理延迟")
                .tags("path", path, "port", port)
                .register(registry);
    }

    @Override
    public void traceSend(WebSocketSession session, String message) {
        sentCounter.increment();
        log.debug("发送消息: sessionId={}, message={}", session.getId(), message);
    }

    @Override
    public void traceReceive(WebSocketSession session, String message) {
        receivedCounter.increment();
        log.debug("接收消息: sessionId={}, message={}", session.getId(), message);
    }

    @Override
    public void traceError(WebSocketSession session, Throwable error) {
        errorCounter.increment();
        log.error("发生错误: sessionId={}", session.getId(), error);
    }

    @Override
    public void traceConnect(WebSocketSession session) {
        connectCounter.increment();
        log.debug("连接建立: sessionId={}, remoteAddress={}", session.getId(), session.getRemoteAddress());
    }

    @Override
    public void traceDisconnect(WebSocketSession session) {
        disconnectCounter.increment();
        log.info("连接断开: sessionId={}", session.getId());
    }

    @Override
    public Timer getMessageLatencyTimer() {
        return messageLatencyTimer;
    }
}
