package io.github.kk01001.netty.trace;

import io.github.kk01001.netty.session.WebSocketSession;
import io.micrometer.core.instrument.Timer;

/**
 * 消息追踪接口
 */
public interface MessageTracer {

    /**
     * 记录发送消息
     */
    void traceSend(WebSocketSession session, String message);

    /**
     * 记录接收消息
     */
    void traceReceive(WebSocketSession session, String message);

    /**
     * 记录错误
     */
    void traceError(WebSocketSession session, Throwable error);

    /**
     * 记录连接建立
     */
    void traceConnect(WebSocketSession session);

    /**
     * 记录连接断开
     */
    void traceDisconnect(WebSocketSession session);

    /**
     * 获取消息延迟计时器
     */
    Timer getMessageLatencyTimer();
} 