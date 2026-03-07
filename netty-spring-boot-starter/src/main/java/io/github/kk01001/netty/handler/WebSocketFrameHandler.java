package io.github.kk01001.netty.handler;

import io.github.kk01001.netty.filter.MessageFilter;
import io.github.kk01001.netty.registry.WebSocketEndpointRegistry;
import io.github.kk01001.netty.session.WebSocketSession;
import io.github.kk01001.netty.trace.MessageTracer;
import io.micrometer.core.instrument.Timer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.github.kk01001.netty.handler.WebSocketHandshakeHandler.SESSION_ATTR;

/**
 * @author kk01001
 * @date 2026-03-07 10:00:00
 * @description WebSocket 帧处理器，处理文本、二进制、Ping/Pong 等消息帧。
 *              Session 从 Channel 属性获取，过滤器在构造时排序。
 */
@Slf4j
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final WebSocketEndpointRegistry registry;
    private final List<MessageFilter> messageFilters;
    private final MessageTracer messageTracer;

    public WebSocketFrameHandler(
            WebSocketEndpointRegistry registry,
            List<MessageFilter> messageFilters,
            MessageTracer messageTracer) {
        super();
        this.registry = registry;
        this.messageFilters = messageFilters;
        this.messageTracer = messageTracer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        WebSocketSession session = ctx.channel().attr(SESSION_ATTR).get();
        if (session == null) {
            log.warn("Session not found for channel: {}, dropping frame", ctx.channel().remoteAddress());
            return;
        }

        session.updateLastActiveTime();

        try {
            Timer.Sample sample = Timer.start();
            handleWebSocketFrame(ctx, session, frame);
            sample.stop(messageTracer.getMessageLatencyTimer());
        } catch (Exception e) {
            messageTracer.traceError(session, e);
            log.error("处理WebSocket消息失败: sessionId={}", session.getId(), e);
            registry.handleError(session, e);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketSession session, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame textFrame) {
            handleTextFrame(session, textFrame);
            return;
        }

        if (frame instanceof BinaryWebSocketFrame binaryFrame) {
            handleBinaryFrame(session, binaryFrame);
            return;
        }

        if (frame instanceof PingWebSocketFrame) {
            log.debug("收到Ping消息: sessionId={}", session.getId());
            session.sendPong();
            return;
        }

        if (frame instanceof PongWebSocketFrame) {
            log.debug("收到Pong消息: sessionId={}", session.getId());
            return;
        }

        log.warn("收到未知类型消息: sessionId={}, frameType={}",
                session.getId(), frame.getClass().getSimpleName());
    }

    private void handleTextFrame(WebSocketSession session, TextWebSocketFrame textFrame) {
        String message = textFrame.text();

        if ("ping".equals(message)) {
            session.sendPongText();
            return;
        }

        messageTracer.traceReceive(session, message);

        for (MessageFilter filter : messageFilters) {
            if (!filter.doFilter(session, message)) {
                log.debug("消息被过滤: sessionId={}, filter={}", session.getId(), filter.getClass().getSimpleName());
                return;
            }
        }

        log.debug("收到文本消息: sessionId={}, message={}", session.getId(), message);
        registry.handleMessage(session, message);
    }

    private void handleBinaryFrame(WebSocketSession session, BinaryWebSocketFrame binaryFrame) {
        log.debug("收到二进制消息: sessionId={}", session.getId());
        ByteBuf content = binaryFrame.content();
        byte[] byteArray = new byte[content.readableBytes()];
        content.readBytes(byteArray);
        registry.handleBinaryMessage(session, byteArray);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        WebSocketSession session = ctx.channel().attr(SESSION_ATTR).get();
        if (session != null) {
            log.error("WebSocket连接异常: sessionId={}", session.getId(), cause);
            registry.handleError(session, cause);
        } else {
            log.error("WebSocket连接异常: channel={}", ctx.channel().remoteAddress(), cause);
        }
        ctx.close();
    }
}
