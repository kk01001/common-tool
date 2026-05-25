package io.github.archer099.redisson.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author archer099
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
     * 双写熔断器配置
     */
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    /**
     * 双写失败重试配置
     */
    private RetryConfig retry = new RetryConfig();

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

    /**
     * 双写失败重试配置
     */
    @Setter
    @Getter
    public static class RetryConfig {

        /**
         * 是否启用重试
         */
        private boolean enabled = true;

        /**
         * 重试队列容量
         */
        private int queueCapacity = 50000;

        /**
         * 最大重试次数
         */
        private int maxRetryCount = 3;

        /**
         * 重试间隔（毫秒）
         */
        private long retryIntervalMs = 5000;

        /**
         * 退避乘数
         */
        private double backoffMultiplier = 2.0;

        /**
         * 最大重试间隔（毫秒）
         */
        private long maxRetryIntervalMs = 60000;

        /**
         * 死信队列容量
         */
        private int deadLetterCapacity = 10000;

        /**
         * 重试工作线程数
         */
        private int workerThreads = 2;

        /**
         * 每次调度处理的最大任务数
         */
        private int batchSize = 100;
    }

    /**
     * 双写熔断器配置
     */
    @Setter
    @Getter
    public static class CircuitBreaker {

        /**
         * 是否启用熔断器
         */
        private boolean enabled = false;

        /**
         * 失败率阈值（百分比），达到此阈值触发熔断
         */
        private double failureRateThreshold = 50.0;

        /**
         * 滑动窗口大小（毫秒），在此时间窗口内统计失败率
         */
        private long slidingWindowSize = 60000;

        /**
         * 最小请求数量，只有达到此数量才会计算失败率
         */
        private int minimumNumberOfCalls = 10;

        /**
         * 熔断打开状态持续时间（毫秒），之后进入半开状态
         */
        private long waitDurationInOpenState = 30000;

        /**
         * 半开状态下允许通过的请求数量
         */
        private int permittedCallsInHalfOpenState = 5;
    }
}
