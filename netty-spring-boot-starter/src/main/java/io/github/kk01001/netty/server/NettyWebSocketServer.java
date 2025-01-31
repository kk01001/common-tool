package io.github.kk01001.netty.server;

import io.github.kk01001.netty.config.NettyWebSocketProperties;
import io.github.kk01001.netty.handler.WebSocketHandler;
import io.github.kk01001.netty.registry.WebSocketEndpointRegistry;
import io.github.kk01001.netty.session.WebSocketSession;
import io.github.kk01001.netty.session.WebSocketSessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.UUID;

@Slf4j
public class NettyWebSocketServer implements InitializingBean, DisposableBean {
    
    private final WebSocketEndpointRegistry registry;
    private final WebSocketSessionManager sessionManager;
    private final NettyWebSocketProperties properties;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    public NettyWebSocketServer(
            WebSocketEndpointRegistry registry,
            WebSocketSessionManager sessionManager,
            NettyWebSocketProperties properties) {
        this.registry = registry;
        this.sessionManager = sessionManager;
        this.properties = properties;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }
    
    @Override
    public void destroy() throws Exception {
        stop();
    }
    
    /**
     * 启动服务器
     */
    public void start() throws Exception {
        bossGroup = new NioEventLoopGroup(properties.getBossThreads());
        workerGroup = new NioEventLoopGroup(properties.getWorkerThreads());
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // HTTP编解码
                            pipeline.addLast(new HttpServerCodec());
                            // 大数据流处理
                            pipeline.addLast(new ChunkedWriteHandler());
                            // HTTP消息聚合
                            pipeline.addLast(new HttpObjectAggregator(properties.getMaxFrameSize()));
                            
                            // WebSocket协议处理
                            pipeline.addLast(new WebSocketServerProtocolHandler(properties.getPath(), 
                                    String.join(",", properties.getSubprotocols()), 
                                    true, 
                                    properties.getMaxFrameSize()));
                            
                            // 创建会话
                            String sessionId = UUID.randomUUID().toString();
                            WebSocketSession session = new WebSocketSession(
                                    sessionId, 
                                    ch, 
                                    properties.getPath(),
                                    ch.remoteAddress().toString(),
                                    sessionManager
                            );
                            sessionManager.addSession(properties.getPath(), session);
                            
                            // 业务处理
                            pipeline.addLast(new WebSocketHandler(registry, session));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            // 绑定端口
            ChannelFuture future = bootstrap.bind(properties.getPort()).sync();
            serverChannel = future.channel();
            
            log.info("WebSocket服务器启动成功: port={}", properties.getPort());
            
            // 等待服务器关闭
            serverChannel.closeFuture().addListener((ChannelFutureListener) channelFuture -> {
                stop();
            });
            
        } catch (Exception e) {
            log.error("WebSocket服务器启动失败", e);
            stop();
            throw e;
        }
    }
    
    /**
     * 停止服务器
     */
    public void stop() {
        try {
            if (serverChannel != null) {
                serverChannel.close();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            log.info("WebSocket服务器已停止");
        } catch (Exception e) {
            log.error("WebSocket服务器停止失败", e);
        }
    }
} 