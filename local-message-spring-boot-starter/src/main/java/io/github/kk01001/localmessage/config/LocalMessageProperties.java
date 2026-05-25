package io.github.archer099.localmessage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 本地消息配置属性
 *
 * @author archer099
 */
@Data
@ConfigurationProperties(prefix = "local-message")
public class LocalMessageProperties {
    
    /**
     * 是否启用本地消息功能
     */
    private boolean enabled = true;
    
    /**
     * 调度器配置
     */
    private Scheduler scheduler = new Scheduler();
    
    /**
     * 线程池配置
     */
    private ThreadPool threadPool = new ThreadPool();
    
    @Data
    public static class Scheduler {
        /**
         * 待处理消息扫描间隔（毫秒）
         */
        private long pendingScanInterval = 30000;
        
        /**
         * 重试消息扫描间隔（毫秒）
         */
        private long retryScanInterval = 60000;
        
        /**
         * 每次扫描的批次大小
         */
        private int batchSize = 100;
    }
    
    @Data
    public static class ThreadPool {
        /**
         * 核心线程数
         */
        private int corePoolSize = 5;
        
        /**
         * 最大线程数
         */
        private int maximumPoolSize = 20;
        
        /**
         * 线程空闲时间（秒）
         */
        private long keepAliveTime = 60;
        
        /**
         * 队列容量
         */
        private int queueCapacity = 1000;
        
        /**
         * 线程名称前缀
         */
        private String threadNamePrefix = "local-message-";

        /**
         * 拒绝策略
         */
        private String rejectedExecutionHandlerClass = "java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy";
    }
}
