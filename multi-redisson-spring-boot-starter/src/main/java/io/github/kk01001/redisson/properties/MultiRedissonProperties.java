package io.github.kk01001.redisson.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2026-01-15 10:00:00
 * @description Redisson 多集群配置属性
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "redisson.multi")
public class MultiRedissonProperties {

    /**
     * 是否启用多集群配置
     */
    private boolean enabled = true;

    /**
     * 主实例名称（用于 @Primary 注入）
     */
    private String primary = "primary";

    /**
     * 备份实例名称（用于双写）
     */
    private String secondary = "secondary";

    /**
     * 是否启用双写
     */
    private boolean dualWriteEnabled = true;

    /**
     * 双写线程池配置
     */
    private DualWriteThreadPool dualWriteThreadPool = new DualWriteThreadPool();

    /**
     * 多个 Redis 实例配置
     * key: 实例名称
     * value: 实例配置
     */
    private Map<String, RedissonInstanceProperties> instances = new LinkedHashMap<>();

    /**
     * 双写线程池配置
     */
    @Setter
    @Getter
    public static class DualWriteThreadPool {

        /**
         * 核心线程数
         */
        private int corePoolSize = 4;

        /**
         * 最大线程数
         */
        private int maxPoolSize = 8;

        /**
         * 队列容量
         */
        private int queueCapacity = 10000;

        /**
         * 线程空闲时间（秒）
         */
        private int keepAliveSeconds = 60;

        /**
         * 线程名称前缀
         */
        private String threadNamePrefix = "dual-write-";

        /**
         * 是否允许核心线程超时
         */
        private boolean allowCoreThreadTimeOut = false;
    }
}
