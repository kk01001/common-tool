package io.github.kk01001.netty.handler;

import io.github.kk01001.netty.auth.WebSocketAuthenticator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class WebSocketAuthHandshakeHandler extends ChannelInboundHandlerAdapter {
    
    public static final AttributeKey<String> USER_ID_ATTR = AttributeKey.valueOf("userId");
    private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_ATTR = 
            AttributeKey.valueOf("HANDSHAKER");
    
    private final WebSocketAuthenticator authenticator;
    private final String websocketPath;
    private final String subprotocols;
    private final boolean allowExtensions;
    private final int maxFramePayloadLength;
    
    public WebSocketAuthHandshakeHandler(
            String websocketPath,
            String subprotocols,
            boolean allowExtensions,
            int maxFramePayloadLength,
            WebSocketAuthenticator authenticator) {
        this.authenticator = authenticator;
        this.websocketPath = websocketPath;
        this.subprotocols = subprotocols;
        this.allowExtensions = allowExtensions;
        this.maxFramePayloadLength = maxFramePayloadLength;
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
        // 如果不是WebSocket握手请求，传递给下一个处理器
        if (!isWebSocketHandshakeRequest(request)) {
            ctx.fireChannelRead(request);
            return;
        }
        
        log.debug("收到WebSocket握手请求: uri={}", request.uri());
        
        // 进行鉴权
        if (authenticator != null) {
            WebSocketAuthenticator.AuthResult authResult = authenticator.authenticate(request);
            if (!authResult.isSuccess()) {
                log.warn("WebSocket鉴权失败: uri={}, message={}", 
                        request.uri(), authResult.getMessage());
                HttpResponse response = new DefaultFullHttpResponse(
                        request.protocolVersion(), HttpResponseStatus.UNAUTHORIZED);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
                response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, 
                        "Basic realm=\"Websocket\"");
                ctx.channel().writeAndFlush(response)
                        .addListener(ChannelFutureListener.CLOSE);
                return;
            }
            // 保存用户ID到channel属性中
            ctx.channel().attr(USER_ID_ATTR).set(authResult.getUserId());
            log.debug("WebSocket鉴权成功: uri={}, userId={}", 
                    request.uri(), authResult.getUserId());
        }
        
        // 处理WebSocket握手
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(request), subprotocols, allowExtensions, maxFramePayloadLength);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            ctx.channel().attr(HANDSHAKER_ATTR).set(handshaker);
            handshaker.handshake(ctx.channel(), request);
        }
    }
    
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 如果是关闭帧，关闭连接
        if (frame instanceof CloseWebSocketFrame) {
            WebSocketServerHandshaker handshaker = ctx.channel().attr(HANDSHAKER_ATTR).get();
            if (handshaker != null) {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            }
            return;
        }
        
        // 其他WebSocket帧传递给下一个处理器
        ctx.fireChannelRead(frame);
    }
    
    private boolean isWebSocketHandshakeRequest(FullHttpRequest request) {
        return request.headers().contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true);
    }
    
    private String getWebSocketLocation(FullHttpRequest req) {
        String host = req.headers().get(HttpHeaderNames.HOST);
        return "ws://" + host + websocketPath;
    }
} 