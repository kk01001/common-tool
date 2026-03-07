package io.github.kk01001.netty.trace;

import io.github.kk01001.netty.session.WebSocketSession;
import io.micrometer.core.instrument.Timer;

/**
 * @author kk01001
 * @date 2026-03-07 10:00:00
 * @description 空操作的消息追踪器，无 Micrometer 依赖时使用
 */
public class NoOpMessageTracer implements MessageTracer {

    private static final Timer NOOP_TIMER = Timer.builder("noop").register(
            new io.micrometer.core.instrument.simple.SimpleMeterRegistry());

    @Override
    public void traceSend(WebSocketSession session, String message) {
    }

    @Override
    public void traceReceive(WebSocketSession session, String message) {
    }

    @Override
    public void traceError(WebSocketSession session, Throwable error) {
    }

    @Override
    public void traceConnect(WebSocketSession session) {
    }

    @Override
    public void traceDisconnect(WebSocketSession session) {
    }

    @Override
    public Timer getMessageLatencyTimer() {
        return NOOP_TIMER;
    }
}
