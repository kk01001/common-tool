package io.github.kk01001.netty.handler;

import io.github.kk01001.netty.auth.WebSocketAuthenticator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.netty.handler.codec.http.HttpHeaderNames;

/**
 * WebSocket鉴权处理器
 */
@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class WebSocketAuthHandler extends ChannelInboundHandlerAdapter {
    
    public static final AttributeKey<String> USER_ID_ATTR = AttributeKey.valueOf("userId");
    
    private final WebSocketAuthenticator authenticator;
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest request) {
            // 保存原始URI
            String originalUri = request.uri();
            log.debug("开始WebSocket鉴权: originalUri={}", originalUri);
            
            // 进行鉴权
            WebSocketAuthenticator.AuthResult authResult = authenticator.authenticate(request);
            
            if (!authResult.isSuccess()) {
                log.warn("WebSocket鉴权失败: uri={}, message={}", 
                        originalUri, authResult.getMessage());
                HttpResponse response = new DefaultFullHttpResponse(
                        request.protocolVersion(), HttpResponseStatus.UNAUTHORIZED);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
                response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, "Basic realm=\"Websocket\"");
                ctx.channel().writeAndFlush(response)
                        .addListener(ChannelFutureListener.CLOSE);
                return;
            }
            
            // 修改URI，移除查询参数
            int queryIndex = originalUri.indexOf('?');
            if (queryIndex > 0) {
                request.setUri(originalUri.substring(0, queryIndex));
            }
            
            log.debug("WebSocket鉴权成功: uri={}, userId={}", 
                    originalUri, authResult.getUserId());
            // 保存用户ID到channel属性中
            ctx.channel().attr(USER_ID_ATTR).set(authResult.getUserId());
        }
        ctx.fireChannelRead(msg);
    }
} 