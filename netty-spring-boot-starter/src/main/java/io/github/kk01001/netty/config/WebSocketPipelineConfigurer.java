package io.github.kk01001.netty.config;

import io.netty.channel.ChannelPipeline;
import io.github.kk01001.netty.session.WebSocketSession;

/**
 * WebSocket Pipeline配置接口
 * 用户可以实现此接口来自定义Pipeline处理链
 */
public interface WebSocketPipelineConfigurer {
    
    /**
     * 配置Pipeline
     * @param pipeline Netty的ChannelPipeline
     * @param session WebSocket会话
     */
    void configurePipeline(ChannelPipeline pipeline, WebSocketSession session);
    
    /**
     * 获取配置器优先级，数字越小优先级越高
     */
    default int getOrder() {
        return 0;
    }
} 