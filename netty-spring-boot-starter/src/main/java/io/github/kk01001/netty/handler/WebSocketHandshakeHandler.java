package io.github.kk01001.netty.handler;

import io.github.kk01001.netty.auth.WebSocketAuthenticator;
import io.github.kk01001.netty.config.NettyWebSocketProperties;
import io.github.kk01001.netty.registry.WebSocketEndpointRegistry;
import io.github.kk01001.netty.session.WebSocketSession;
import io.github.kk01001.netty.session.WebSocketSessionManager;
import io.github.kk01001.netty.trace.MessageTracer;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author kk01001
 * @date 2026-03-07 10:00:00
 * @description WebSocket 握手处理器，统一处理 HTTP 升级、鉴权、Session 创建。
 *              每个 Channel 创建一个新实例（非 Sharable）。
 */
@Slf4j
public class WebSocketHandshakeHandler extends ChannelInboundHandlerAdapter {

    public static final AttributeKey<WebSocketSession> SESSION_ATTR = AttributeKey.valueOf("ws-session");
    public static final AttributeKey<String> SESSION_ID_ATTR = AttributeKey.valueOf("ws-session-id");

    private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_ATTR =
            AttributeKey.valueOf("ws-handshaker");

    private final WebSocketAuthenticator authenticator;
    private final boolean authEnabled;
    private final boolean sslEnabled;
    private final String subprotocols;
    private final boolean allowExtensions;
    private final int maxFramePayloadLength;
    private final WebSocketSessionManager sessionManager;
    private final WebSocketEndpointRegistry registry;
    private final MessageTracer messageTracer;

    public WebSocketHandshakeHandler(
            NettyWebSocketProperties properties,
            WebSocketAuthenticator authenticator,
            WebSocketSessionManager sessionManager,
            WebSocketEndpointRegistry registry,
            MessageTracer messageTracer) {
        this.authenticator = authenticator;
        this.authEnabled = properties.isAuthEnabled();
        this.sslEnabled = properties.getSsl().isEnabled();
        this.subprotocols = String.join(",", properties.getSubprotocols());
        this.allowExtensions = true;
        this.maxFramePayloadLength = properties.getMaxFrameSize();
        this.sessionManager = sessionManager;
        this.registry = registry;
        this.messageTracer = messageTracer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest request) {
            handleHttpRequest(ctx, request);
        } else if (msg instanceof WebSocketFrame frame) {
            handleWebSocketFrame(ctx, frame);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!isWebSocketUpgrade(request)) {
            ctx.fireChannelRead(request);
            return;
        }

        String uri = request.uri();
        String path = new QueryStringDecoder(uri).path();
        log.debug("收到WebSocket握手请求: uri={}, path={}", uri, path);

        if (!registry.hasEndpoint(path)) {
            log.warn("未找到匹配的WebSocket端点: path={}", path);
            sendHttpResponse(ctx, request, HttpResponseStatus.NOT_FOUND);
            return;
        }

        final String userId;
        if (authEnabled && authenticator != null) {
            WebSocketAuthenticator.AuthResult authResult = authenticator.authenticate(request);
            if (!authResult.isSuccess()) {
                log.warn("WebSocket鉴权失败: uri={}, message={}", uri, authResult.getMessage());
                sendHttpResponse(ctx, request, HttpResponseStatus.UNAUTHORIZED);
                return;
            }
            userId = authResult.getUserId();
            log.debug("WebSocket鉴权成功: uri={}, userId={}", uri, userId);
        } else {
            userId = null;
        }

        if (!sessionManager.canAcceptConnection()) {
            log.warn("连接数已达上限，拒绝连接: uri={}", uri);
            sendHttpResponse(ctx, request, HttpResponseStatus.SERVICE_UNAVAILABLE);
            return;
        }

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(request), subprotocols, allowExtensions, maxFramePayloadLength);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            return;
        }

        ctx.channel().attr(HANDSHAKER_ATTR).set(handshaker);
        handshaker.handshake(ctx.channel(), request).addListener(future -> {
            if (future.isSuccess()) {
                onHandshakeComplete(ctx, path, userId);
            } else {
                log.error("WebSocket握手失败: uri={}", uri, future.cause());
                ctx.close();
            }
        });
    }

    private void onHandshakeComplete(ChannelHandlerContext ctx, String path, String userId) {
        String sessionId = UUID.randomUUID().toString();
        String remoteAddress = ctx.channel().remoteAddress() != null
                ? ctx.channel().remoteAddress().toString() : "unknown";

        WebSocketSession session = new WebSocketSession(
                sessionId, ctx.channel(), path, remoteAddress, sessionManager, messageTracer);
        if (userId != null) {
            session.setUserId(userId);
        }

        ctx.channel().attr(SESSION_ATTR).set(session);
        ctx.channel().attr(SESSION_ID_ATTR).set(sessionId);

        sessionManager.addSession(session);
        messageTracer.traceConnect(session);
        registry.handleOpen(session);

        log.debug("WebSocket连接建立: sessionId={}, path={}, userId={}", sessionId, path, userId);
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            WebSocketServerHandshaker handshaker = ctx.channel().attr(HANDSHAKER_ATTR).get();
            if (handshaker != null) {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            }
            return;
        }
        ctx.fireChannelRead(frame);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        WebSocketSession session = ctx.channel().attr(SESSION_ATTR).get();
        if (session != null) {
            messageTracer.traceDisconnect(session);
            registry.handleClose(session);
            sessionManager.removeSession(session.getId());
            ctx.channel().attr(SESSION_ATTR).set(null);
            log.debug("WebSocket连接关闭: sessionId={}", session.getId());
        }
    }

    private boolean isWebSocketUpgrade(FullHttpRequest request) {
        return request.headers().contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true);
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        String host = req.headers().get(HttpHeaderNames.HOST);
        String protocol = sslEnabled ? "wss" : "ws";
        String path = new QueryStringDecoder(req.uri()).path();
        return protocol + "://" + host + path;
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) {
        HttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), status);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
