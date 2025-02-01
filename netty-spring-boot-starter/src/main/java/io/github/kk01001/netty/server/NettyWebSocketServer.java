package io.github.kk01001.netty.server;

import io.github.kk01001.netty.auth.WebSocketAuthenticator;
import io.github.kk01001.netty.config.NettyWebSocketProperties;
import io.github.kk01001.netty.config.WebSocketPipelineConfigurer;
import io.github.kk01001.netty.config.ChannelOptionCustomizer;
import io.github.kk01001.netty.handler.WebSocketAuthHandshakeHandler;
import io.github.kk01001.netty.handler.WebSocketHandler;
import io.github.kk01001.netty.handler.WebSocketHeartbeatHandler;
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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.net.ssl.SSLEngine;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyWebSocketServer implements InitializingBean, DisposableBean {
    
    private final WebSocketEndpointRegistry registry;
    private final WebSocketSessionManager sessionManager;
    private final NettyWebSocketProperties properties;
    private final WebSocketAuthenticator authenticator;
    private final WebSocketAuthHandshakeHandler handshakeHandler;
    private final WebSocketHeartbeatHandler heartbeatHandler;
    private final List<WebSocketPipelineConfigurer> pipelineConfigurers;
    private final List<ChannelOptionCustomizer> optionCustomizers;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private SslContext sslContext;
    
    public NettyWebSocketServer(
            WebSocketEndpointRegistry registry,
            WebSocketSessionManager sessionManager,
            NettyWebSocketProperties properties,
            WebSocketAuthenticator authenticator,
            List<WebSocketPipelineConfigurer> pipelineConfigurers,
            List<ChannelOptionCustomizer> optionCustomizers) {
        this.registry = registry;
        this.sessionManager = sessionManager;
        this.properties = properties;
        this.authenticator = authenticator;
        this.pipelineConfigurers = pipelineConfigurers;
        this.optionCustomizers = optionCustomizers;
        this.handshakeHandler = new WebSocketAuthHandshakeHandler(
                properties.getPath(),
                String.join(",", properties.getSubprotocols()),
                true,
                properties.getMaxFrameSize(),
                authenticator
        );
        this.heartbeatHandler = new WebSocketHeartbeatHandler();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化SSL上下文
        if (properties.getSsl().isEnabled()) {
            initSslContext();
        }
        start();
    }
    
    @Override
    public void destroy() throws Exception {
        stop();
    }
    
    private void initSslContext() throws Exception {
        File certFile = new File(properties.getSsl().getCertPath());
        File keyFile = new File(properties.getSsl().getKeyPath());
        String keyPassword = properties.getSsl().getKeyPassword();
        
        sslContext = SslContextBuilder.forServer(certFile, keyFile, keyPassword)
                .build();
        
        log.info("SSL上下文初始化成功");
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
                            
                            // 添加SSL处理器
                            if (properties.getSsl().isEnabled() && sslContext != null) {
                                SSLEngine engine = sslContext.newEngine(ch.alloc());
                                engine.setUseClientMode(false);
                                pipeline.addFirst("ssl", new SslHandler(engine));
                            }
                            
                            // HTTP编解码
                            pipeline.addLast(new HttpServerCodec());
                            // 大数据流处理
                            pipeline.addLast(new ChunkedWriteHandler());
                            // HTTP消息聚合
                            pipeline.addLast(new HttpObjectAggregator(properties.getMaxFrameSize()));

                            // WebSocket握手和鉴权处理
                            pipeline.addLast(handshakeHandler);
                            
                            // 心跳处理
                            if (properties.getHeartbeat().isEnabled()) {
                                pipeline.addLast(new IdleStateHandler(
                                    properties.getHeartbeat().getReaderIdleTime(),
                                    properties.getHeartbeat().getWriterIdleTime(),
                                    0,
                                    TimeUnit.SECONDS
                                ));
                                pipeline.addLast(heartbeatHandler);
                            }
                            
                            // 创建会话
                            String sessionId = UUID.randomUUID().toString();
                            String userId = null;
                            if (properties.isAuthEnabled()) {
                                userId = ch.attr(WebSocketAuthHandshakeHandler.USER_ID_ATTR).get();
                            }
                            WebSocketSession session = new WebSocketSession(
                                    sessionId, 
                                    ch, 
                                    properties.getPath(),
                                    ch.remoteAddress().toString(),
                                    sessionManager,
                                    userId
                            );
                            sessionManager.addSession(properties.getPath(), session);

                            // 调用自定义Pipeline配置
                            if (pipelineConfigurers != null) {
                                // 按优先级排序
                                pipelineConfigurers.sort(Comparator.comparingInt(WebSocketPipelineConfigurer::getOrder));
                                for (WebSocketPipelineConfigurer configurer : pipelineConfigurers) {
                                    configurer.configurePipeline(pipeline, session);
                                }
                            }
                            
                            // 业务处理
                            pipeline.addLast("webSocketHandler", new WebSocketHandler(registry, session));
                        }
                    });
            
            // 应用默认选项
            applyDefaultOptions(bootstrap);
            
            // 应用自定义选项
            if (optionCustomizers != null) {
                optionCustomizers.sort(Comparator.comparingInt(ChannelOptionCustomizer::getOrder));
                for (ChannelOptionCustomizer customizer : optionCustomizers) {
                    customizer.customizeServerOptions(bootstrap);
                    customizer.customizeChildOptions(bootstrap);
                }
            }

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
    
    private void applyDefaultOptions(ServerBootstrap bootstrap) {
        // 应用服务端选项
        bootstrap.option(ChannelOption.SO_BACKLOG, properties.getServerOptions().getSoBacklog());
        
        // 应用客户端选项
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, properties.getChildOptions().isSoKeepalive())
                .childOption(ChannelOption.TCP_NODELAY, properties.getChildOptions().isTcpNodelay())
                .childOption(ChannelOption.SO_RCVBUF, properties.getChildOptions().getSoRcvbuf())
                .childOption(ChannelOption.SO_SNDBUF, properties.getChildOptions().getSoSndbuf());
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