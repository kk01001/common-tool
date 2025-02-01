package io.github.kk01001.netty.config;

import io.netty.bootstrap.ServerBootstrap;

/**
 * Channel选项自定义接口
 */
public interface ChannelOptionCustomizer {

    /**
     * 自定义ServerBootstrap的option选项
     */
    void customizeServerOptions(ServerBootstrap bootstrap);

    /**
     * 自定义ServerBootstrap的childOption选项
     */
    void customizeChildOptions(ServerBootstrap bootstrap);

    /**
     * 获取优先级，数字越小优先级越高
     */
    default int getOrder() {
        return 0;
    }
} 