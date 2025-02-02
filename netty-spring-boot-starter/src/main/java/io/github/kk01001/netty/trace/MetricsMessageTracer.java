package io.github.kk01001.netty.trace;

import io.github.kk01001.netty.config.NettyWebSocketProperties;
import io.github.kk01001.netty.session.WebSocketSession;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricsMessageTracer implements MessageTracer {

    private final MeterRegistry registry;
    private final NettyWebSocketProperties properties;

    public MetricsMessageTracer(MeterRegistry registry, NettyWebSocketProperties properties) {
        this.registry = registry;
        this.properties = properties;
    }

    private Counter createCounter(String name, String description) {
        return Counter.builder(name)
                .description(description)
                .tags("path", properties.getPath(), 
                     "port", String.valueOf(properties.getPort()))
                .register(registry);
    }

    private Timer createTimer(String name, String description) {
        return Timer.builder(name)
                .description(description)
                .tags("path", properties.getPath(), 
                     "port", String.valueOf(properties.getPort()))
                .register(registry);
    }

    @Override
    public void traceSend(WebSocketSession session, String message) {
        createCounter("websocket.messages.sent", "WebSocket发送消息计数").increment();
        log.debug("发送消息: sessionId={}, message={}", session.getId(), message);
    }

    @Override
    public void traceReceive(WebSocketSession session, String message) {
        createCounter("websocket.messages.received", "WebSocket接收消息计数").increment();
        log.debug("接收消息: sessionId={}, message={}", session.getId(), message);
    }

    @Override
    public void traceError(WebSocketSession session, Throwable error) {
        createCounter("websocket.errors", "WebSocket错误计数").increment();
        log.error("发生错误: sessionId={}", session.getId(), error);
    }

    @Override
    public void traceConnect(WebSocketSession session) {
        createCounter("websocket.connections", "WebSocket连接计数").increment();
        log.debug("连接建立: sessionId={}, remoteAddress={}",
                session.getId(), session.getChannel().remoteAddress());
    }

    @Override
    public void traceDisconnect(WebSocketSession session) {
        createCounter("websocket.disconnections", "WebSocket断开连接计数").increment();
        log.info("连接断开: sessionId={}", session.getId());
    }

    @Override
    public Timer getMessageLatencyTimer() {
        return createTimer("websocket.message.latency", "WebSocket消息处理延迟");
    }
} 