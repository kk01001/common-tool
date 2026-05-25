package io.github.archer099.netty.server;

import io.github.archer099.netty.auth.WebSocketAuthenticator;
import io.github.archer099.netty.cluster.WebSocketClusterManager;
import io.github.archer099.netty.config.ChannelOptionCustomizer;
import io.github.archer099.netty.config.NettyWebSocketProperties;
import io.github.archer099.netty.config.WebSocketPipelineConfigurer;
import io.github.archer099.netty.filter.MessageFilter;
import io.github.archer099.netty.handler.WebSocketFrameHandler;
import io.github.archer099.netty.handler.WebSocketHandshakeHandler;
import io.github.archer099.netty.handler.WebSocketHeartbeatHandler;
import io.github.archer099.netty.registry.WebSocketEndpointRegistry;
import io.github.archer099.netty.session.WebSocketSessionManager;
import io.github.archer099.netty.trace.MessageTracer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
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
import java.util.concurrent.TimeUnit;

/**
 * @author archer099
 * @date 2026-03-07 10:00:00
 * @description Netty WebSocket 服务器，管理 Netty 生命周期和 Pipeline 配置
 */
@Slf4j
public class NettyWebSocketServer implements InitializingBean, DisposableBean {

    private final WebSocketEndpointRegistry registry;
    private final WebSocketSessionManager sessionManager;
    private final NettyWebSocketProperties properties;
    private final WebSocketAuthenticator authenticator;
    private final List<WebSocketPipelineConfigurer> pipelineConfigurers;
    private final List<ChannelOptionCustomizer> optionCustomizers;
    private final List<MessageFilter> messageFilters;
    private final MessageTracer messageTracer;
    private final WebSocketClusterManager clusterManager;
    private final WebSocketHeartbeatHandler heartbeatHandler;

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
            List<ChannelOptionCustomizer> optionCustomizers,
            List<MessageFilter> messageFilters,
            MessageTracer messageTracer,
            WebSocketClusterManager clusterManager) {
        this.registry = registry;
        this.sessionManager = sessionManager;
        this.properties = properties;
        this.authenticator = authenticator;
        this.pipelineConfigurers = pipelineConfigurers;
        this.optionCustomizers = optionCustomizers;
        this.messageFilters = messageFilters;
        this.messageTracer = messageTracer;
        this.clusterManager = clusterManager;
        this.heartbeatHandler = new WebSocketHeartbeatHandler();

        if (messageFilters != null) {
            messageFilters.sort(Comparator.comparingInt(MessageFilter::getOrder));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (properties.getSsl().isEnabled()) {
            initSslContext();
        }
        start();
        if (clusterManager != null) {
            clusterManager.init();
        }

        sessionManager.setCloseCallback(session -> registry.handleClose(session));
    }

    @Override
    public void destroy() throws Exception {
        stop();
        if (clusterManager != null) {
            clusterManager.destroy();
        }
    }

    private void initSslContext() throws Exception {
        File certFile = new File(properties.getSsl().getCertPath());
        File keyFile = new File(properties.getSsl().getKeyPath());
        String keyPassword = properties.getSsl().getKeyPassword();

        sslContext = SslContextBuilder.forServer(certFile, keyFile, keyPassword).build();
        log.info("SSL上下文初始化成功");
    }

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

                            if (properties.getSsl().isEnabled() && sslContext != null) {
                                SSLEngine engine = sslContext.newEngine(ch.alloc());
                                engine.setUseClientMode(false);
                                pipeline.addFirst("ssl", new SslHandler(engine));
                            }

                            pipeline.addLast("httpCodec", new HttpServerCodec());
                            pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
                            pipeline.addLast("httpAggregator", new HttpObjectAggregator(properties.getMaxFrameSize()));

                            pipeline.addLast("handshake", new WebSocketHandshakeHandler(
                                    properties, authenticator, sessionManager, registry, messageTracer));
                            pipeline.addLast("frameAggregator",
                                    new io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator(
                                            properties.getMaxFrameSize()));

                            if (properties.getHeartbeat().isEnabled()) {
                                pipeline.addLast("idleState", new IdleStateHandler(
                                        properties.getHeartbeat().getReaderIdleTime(),
                                        properties.getHeartbeat().getWriterIdleTime(),
                                        0, TimeUnit.SECONDS));
                                pipeline.addLast("heartbeat", heartbeatHandler);
                            }

                            if (pipelineConfigurers != null) {
                                pipelineConfigurers.sort(Comparator.comparingInt(WebSocketPipelineConfigurer::getOrder));
                                for (WebSocketPipelineConfigurer configurer : pipelineConfigurers) {
                                    configurer.configurePipeline(pipeline);
                                }
                            }

                            pipeline.addLast("frameHandler", new WebSocketFrameHandler(
                                    registry, messageFilters, messageTracer));
                        }
                    });

            applyDefaultOptions(bootstrap);

            if (optionCustomizers != null) {
                optionCustomizers.sort(Comparator.comparingInt(ChannelOptionCustomizer::getOrder));
                for (ChannelOptionCustomizer customizer : optionCustomizers) {
                    customizer.customizeServerOptions(bootstrap);
                    customizer.customizeChildOptions(bootstrap);
                }
            }

            serverChannel = bootstrap.bind(properties.getPort()).sync().channel();
            log.info("WebSocket服务器启动成功: port={}", properties.getPort());

        } catch (Exception e) {
            log.error("WebSocket服务器启动失败", e);
            stop();
            throw e;
        }
    }

    private void applyDefaultOptions(ServerBootstrap bootstrap) {
        bootstrap.option(ChannelOption.SO_BACKLOG, properties.getServerOptions().getSoBacklog());
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, properties.getChildOptions().isSoKeepalive())
                .childOption(ChannelOption.TCP_NODELAY, properties.getChildOptions().isTcpNodelay())
                .childOption(ChannelOption.SO_RCVBUF, properties.getChildOptions().getSoRcvbuf())
                .childOption(ChannelOption.SO_SNDBUF, properties.getChildOptions().getSoSndbuf());
    }

    public void stop() {
        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("关闭服务器通道被中断");
        } catch (Exception e) {
            log.error("关闭服务器通道失败", e);
        }

        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS).sync();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS).sync();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("关闭EventLoopGroup被中断");
        } catch (Exception e) {
            log.error("关闭EventLoopGroup失败", e);
        }

        log.info("WebSocket服务器已停止");
    }
}
