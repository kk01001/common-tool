package io.github.kk01001.threadpool.thirdparty;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 第三方线程池配置属性
 * 
 * @author kk01001
 */
@Data
@ConfigurationProperties(prefix = "dynamic-threadpool.third-party")
public class ThirdPartyThreadPoolProperties {
    
    /**
     * 是否启用第三方线程池管理
     */
    private Boolean enabled = false;
    
    /**
     * 第三方线程池配置列表
     */
    private List<PoolConfig> pools = new ArrayList<>();
    
    /**
     * 单个线程池配置
     */
    @Data
    public static class PoolConfig {
        /**
         * 线程池类型
         */
        private ThirdPartyPoolType type;
        
        /**
         * 线程池名称（不包含类型前缀）
         * 默认为 "main"
         */
        private String name = "main";
        
        /**
         * 是否启用
         */
        private Boolean enabled = true;
        
        /**
         * 最大线程数
         */
        private Integer maxThreads;
        
        /**
         * 最小线程数
         */
        private Integer minThreads;
        
        /**
         * 队列容量
         */
        private Integer queueCapacity;
        
        /**
         * 最大连接数
         */
        private Integer maxConnections;
        
        /**
         * 连接超时（毫秒）
         */
        private Long connectionTimeout;
        
        /**
         * Keep-Alive 时间（秒）
         */
        private Long keepAliveTime;
        
        /**
         * 转换为 ThirdPartyThreadPoolConfig
         */
        public ThirdPartyThreadPoolConfig toConfig() {
            return ThirdPartyThreadPoolConfig.builder()
                    .poolName(type.getCode() + ":" + name)
                    .maxThreads(maxThreads)
                    .minThreads(minThreads)
                    .queueCapacity(queueCapacity)
                    .maxConnections(maxConnections)
                    .connectionTimeout(connectionTimeout)
                    .keepAliveTime(keepAliveTime)
                    .build();
        }
    }
}
