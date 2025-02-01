package io.github.kk01001.examples.config;

import io.github.kk01001.netty.auth.WebSocketAuthenticator;
import io.github.kk01001.netty.config.ChannelOptionCustomizer;
import io.github.kk01001.netty.config.WebSocketPipelineConfigurer;
import io.github.kk01001.netty.session.WebSocketSession;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
public class WebSocketConfig {
    
    @Bean
    public WebSocketAuthenticator webSocketAuthenticator() {
        return request -> {
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            String token = decoder.parameters().getOrDefault("token", List.of("")).get(0);
            
            if (!StringUtils.hasText(token)) {
                return WebSocketAuthenticator.AuthResult.fail("Missing token");
            }
            
            // 这里实现你的鉴权逻辑
            if (isValidToken(token)) {
                String userId = getUserIdFromToken(token);
                return WebSocketAuthenticator.AuthResult.success(userId);
            }
            
            return WebSocketAuthenticator.AuthResult.fail("Invalid token");
        };
    }

    private String getUserIdFromToken(String token) {
        return token;
    }

    private boolean isValidToken(String token) {
        return true;
    }

    @Bean
    public WebSocketPipelineConfigurer customPipelineConfigurer() {
        return new WebSocketPipelineConfigurer() {
            @Override
            public void configurePipeline(ChannelPipeline pipeline, WebSocketSession session) {
                // 添加自定义处理器
                pipeline.addLast("customHandler", new CustomHandler());
            }

            @Override
            public int getOrder() {
                return 1;
            }
        };
    }

    @Bean
    public ChannelOptionCustomizer customChannelOptions() {
        return new ChannelOptionCustomizer() {
            @Override
            public void customizeServerOptions(ServerBootstrap bootstrap) {
                bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
            }

            @Override
            public void customizeChildOptions(ServerBootstrap bootstrap) {
                bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                        new WriteBufferWaterMark(32768, 65536));
            }
        };
    }

    // 自定义处理器示例
    private static class CustomHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 自定义处理逻辑
            super.channelRead(ctx, msg);
        }
    }
} 