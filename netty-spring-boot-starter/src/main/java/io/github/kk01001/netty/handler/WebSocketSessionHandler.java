package io.github.kk01001.netty.handler;

import io.github.kk01001.netty.config.NettyWebSocketProperties;
import io.github.kk01001.netty.session.WebSocketSession;
import io.github.kk01001.netty.session.WebSocketSessionManager;
import io.github.kk01001.netty.trace.MessageTracer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class WebSocketSessionHandler extends ChannelInboundHandlerAdapter {

    public static final AttributeKey<String> SESSION_ID_ATTR = AttributeKey.valueOf("sessionId-");

    public static final AttributeKey<WebSocketSession> SESSION_ATTRIBUTE_KEY_ATTR = AttributeKey.valueOf("session-");

    private final NettyWebSocketProperties properties;
    private final WebSocketSessionManager sessionManager;
    private final MessageTracer messageTracer;

    public WebSocketSessionHandler(
            NettyWebSocketProperties properties,
            WebSocketSessionManager sessionManager,
            MessageTracer messageTracer) {
        this.properties = properties;
        this.sessionManager = sessionManager;
        this.messageTracer = messageTracer;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 创建会话
        String sessionId = UUID.randomUUID().toString();
        WebSocketSession session = getWebSocketSession(ctx.channel());
        sessionManager.addSession(session);
        ctx.channel().attr(SESSION_ATTRIBUTE_KEY_ATTR).set(session);
        ctx.channel().attr(SESSION_ID_ATTR).set(session.getId());
        log.debug("创建WebSocket会话: sessionId={}, userId={}", sessionId, session.getUserId());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        String sessionId = ctx.channel().attr(SESSION_ID_ATTR).get();
        try {
            sessionManager.removeSession(sessionId);
            log.debug("WebSocket连接关闭: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("处理连接关闭失败: sessionId={}", sessionId, e);
        }
    }

    private WebSocketSession getWebSocketSession(Channel channel) {
        String sessionId = UUID.randomUUID().toString();
        WebSocketSession session = new WebSocketSession(
                sessionId,
                channel,
                channel.remoteAddress().toString(),
                sessionManager,
                messageTracer
        );
        session.setUserId(channel.attr(WebSocketAuthHandshakeHandler.USER_ID_ATTR).get());
        return session;
    }
}