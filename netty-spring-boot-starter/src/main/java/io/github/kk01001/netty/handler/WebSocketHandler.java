package io.github.kk01001.netty.handler;

import io.github.kk01001.netty.filter.MessageFilter;
import io.github.kk01001.netty.registry.WebSocketEndpointRegistry;
import io.github.kk01001.netty.session.WebSocketSession;
import io.github.kk01001.netty.trace.MessageTracer;
import io.micrometer.core.instrument.Timer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

import static io.github.kk01001.netty.handler.WebSocketSessionHandler.SESSION_ATTRIBUTE_KEY_ATTR;

/**
 * WebSocket消息处理器
 */
@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    
    private final WebSocketEndpointRegistry registry;
    private WebSocketSession session;
    private final List<MessageFilter> messageFilters;
    private final MessageTracer messageTracer;

    public WebSocketHandler(
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
        try {
            Timer.Sample sample = Timer.start();
            handleWebSocketFrame(ctx, frame);
            sample.stop(messageTracer.getMessageLatencyTimer());
        } catch (Exception e) {
            messageTracer.traceError(session, e);
            log.error("处理WebSocket消息失败: sessionId={}", session.getId(), e);
            registry.handleError(session, e);
        }
    }
    
    /**
     * 处理WebSocket消息
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 处理文本消息
        if (frame instanceof TextWebSocketFrame textFrame) {
            String message = textFrame.text();
            if ("ping".equals(message)) {
                session.sendPongText();
                return;
            }
            
            // 记录接收消息
            messageTracer.traceReceive(session, message);
            
            // 应用消息过滤器
            if (messageFilters != null) {
                // 按优先级排序
                messageFilters.sort(Comparator.comparingInt(MessageFilter::getOrder));
                for (MessageFilter filter : messageFilters) {
                    if (!filter.doFilter(session, message)) {
                        log.debug("消息被过滤: sessionId={}, message={}", session.getId(), message);
                        return;
                    }
                }
            }
            
            log.debug("收到文本消息: sessionId={}, message={}", session.getId(), message);
            registry.handleMessage(session, message);
            return;
        }
        
        // 处理二进制消息
        if (frame instanceof BinaryWebSocketFrame socketFrame) {
            log.debug("收到二进制消息: sessionId={}", session.getId());
            ByteBuf content = socketFrame.content();
            byte[] byteArray = new byte[content.readableBytes()];
            content.readBytes(byteArray);
            registry.handleBinaryMessage(session, byteArray);
            return;
        }
        
        // 处理Ping消息
        if (frame instanceof PingWebSocketFrame) {
            log.debug("收到Ping消息: sessionId={}", session.getId());
            session.sendPong();
            return;
        }
        
        // 处理Pong消息
        if (frame instanceof PongWebSocketFrame) {
            log.debug("收到Pong消息: sessionId={}", session.getId());
            return;
        }
        
        // 处理关闭消息
        if (frame instanceof CloseWebSocketFrame) {
            log.debug("收到关闭消息: sessionId={}", session.getId());
            ctx.close();
            return;
        }
        
        // 处理其他类型消息
        log.warn("收到未知类型消息: sessionId={}, frameType={}", 
                session.getId(), frame.getClass().getSimpleName());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        /*try {
            this.session = ctx.channel().attr(SESSION_ATTRIBUTE_KEY_ATTR).get();
            messageTracer.traceConnect(session);
            log.debug("WebSocket连接建立: sessionId={}, remoteAddress={}",
                    session.getId(), ctx.channel().remoteAddress());
            registry.handleOpen(session);
            super.channelActive(ctx);
        } catch (Exception e) {
            log.error("处理连接建立失败: sessionId={}", session.getId(), e);
            ctx.close();
        }*/
        super.channelActive(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        try {
            this.session = ctx.channel().attr(SESSION_ATTRIBUTE_KEY_ATTR).get();
            messageTracer.traceConnect(session);
            log.debug("WebSocket连接建立: sessionId={}, remoteAddress={}", 
                    session.getId(), ctx.channel().remoteAddress());
            // registry.handleOpen(session);
        } catch (Exception e) {
            log.error("处理连接建立失败: sessionId={}", session.getId(), e);
            ctx.close();
        }
    }
    
    /*@Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        try {
            messageTracer.traceDisconnect(session);
            log.debug("WebSocket连接关闭: sessionId={}", session.getId());
            registry.handleClose(session);
        } catch (Exception e) {
            log.error("处理连接关闭失败: sessionId={}", session.getId(), e);
        }
    }*/
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        try {
            log.error("WebSocket连接异常: sessionId={}", session.getId(), cause);
            registry.handleError(session, cause);
        } finally {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
            log.debug("WebSocket连接断开: sessionId={}", session.getId());
            registry.handleClose(session);
        } catch (Exception e) {
            log.error("处理连接断开失败: sessionId={}", session.getId(), e);
        }
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            // WebSocket握手完成
            WebSocketServerProtocolHandler.HandshakeComplete complete = 
                    (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            log.debug("WebSocket握手完成: sessionId={}, protocol={}", 
                    session.getId(), complete.selectedSubprotocol());
        }
    }

}