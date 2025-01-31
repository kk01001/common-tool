package io.github.kk01001.netty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "netty.websocket")
public class NettyWebSocketProperties {
    /**
     * 服务端口
     */
    private int port = 8081;
    
    /**
     * boss线程数
     */
    private int bossThreads = 1;
    
    /**
     * worker线程数，0表示使用CPU核心数*2
     */
    private int workerThreads = 0;
    
    /**
     * 最大帧大小
     */
    private int maxFrameSize = 65536;
    
    /**
     * WebSocket路径
     */
    private String path = "/";
    
    /**
     * WebSocket子协议
     */
    private String[] subprotocols = new String[0];
    
    /**
     * 集群配置
     */
    private Cluster cluster = new Cluster();
    
    /**
     * 会话超时时间
     */
    private Duration sessionTimeout = Duration.ofMinutes(30);
    
    @Data
    public static class Cluster {
        /**
         * 是否启用集群
         */
        private boolean enabled = false;
        
        /**
         * Redis Topic
         */
        private String topic = "ws:broadcast";
        
        /**
         * 集群会话超时时间
         */
        private Duration sessionTimeout = Duration.ofHours(1);
    }
} 