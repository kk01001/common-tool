package io.github.archer099.threadpool.custom.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态线程池配置属性
 *
 * @author archer099
 */
@Data
@ConfigurationProperties(prefix = "dynamic-threadpool")
public class DynamicThreadPoolProperties {

    /**
     * 是否启用动态线程池
     */
    private Boolean enabled = true;

    /**
     * 全局默认配置
     */
    private GlobalConfig global = new GlobalConfig();

    /**
     * 各个线程池的配置
     */
    private Map<String, ThreadPoolConfigProperties> pools = new HashMap<>();

    /**
     * 监控配置
     */
    private MonitorConfig monitor = new MonitorConfig();

    /**
     * 告警配置
     */
    private AlarmConfig alarm = new AlarmConfig();

    /**
     * 全局默认配置
     */
    @Data
    public static class GlobalConfig {
        /**
         * 默认核心线程数
         */
        private Integer corePoolSize = 5;

        /**
         * 默认最大线程数
         */
        private Integer maxPoolSize = 10;

        /**
         * 默认队列容量
         */
        private Integer queueCapacity = 100;

        /**
         * 默认线程存活时间
         */
        private Duration keepAliveTime = Duration.ofSeconds(60);

        /**
         * 默认线程名称前缀
         */
        private String threadNamePrefix = "dynamic-pool-";

        /**
         * 是否允许核心线程超时
         */
        private Boolean allowCoreThreadTimeout = false;

        /**
         * 是否启用 TTL (TransmittableThreadLocal)
         */
        private Boolean enableTtl = false;
    }

    /**
     * 线程池配置
     */
    @Data
    public static class ThreadPoolConfigProperties {
        /**
         * 核心线程数
         */
        private Integer corePoolSize;

        /**
         * 最大线程数
         */
        private Integer maxPoolSize;

        /**
         * 队列容量
         */
        private Integer queueCapacity;

        /**
         * 队列类型
         */
        private QueueType queueType = QueueType.LINKED_BLOCKING_QUEUE;

        /**
         * 线程存活时间
         */
        private Duration keepAliveTime;

        /**
         * 线程名称前缀
         */
        private String threadNamePrefix;

        /**
         * 拒绝策略
         */
        private RejectedPolicyType rejectedPolicy = RejectedPolicyType.ABORT_POLICY;

        /**
         * 是否允许核心线程超时
         */
        private Boolean allowCoreThreadTimeout;

        /**
         * 是否启用 TTL
         */
        private Boolean enableTtl;

        /**
         * 是否在项目启动时自动初始化线程池
         * 默认为 false，需要显式配置为 true 才会自动初始化
         */
        private Boolean autoInit = false;
    }

    /**
     * 监控配置
     */
    @Data
    public static class MonitorConfig {
        /**
         * 是否启用监控
         */
        private Boolean enabled = true;

        /**
         * 是否启用 Actuator 端点
         */
        private Boolean enableActuator = true;

        /**
         * 是否启用指标收集
         */
        private Boolean enableMetrics = true;

        /**
         * 监控采集间隔
         */
        private Duration collectInterval = Duration.ofSeconds(5);
    }

    /**
     * 告警配置
     */
    @Data
    public static class AlarmConfig {
        /**
         * 是否启用告警
         */
        private Boolean enabled = true;

        /**
         * 队列使用率阈值（0-1）
         */
        private Double queueUsageThreshold = 0.8;

        /**
         * 活跃线程比例阈值（0-1）
         */
        private Double activeThreadRatioThreshold = 0.9;

        /**
         * 拒绝次数阈值
         */
        private Long rejectCountThreshold = 100L;

        /**
         * 告警间隔时间（防止告警风暴）
         */
        private Duration alarmInterval = Duration.ofMinutes(5);

        /**
         * 告警通知渠道类型
         */
        private AlarmChannelType channelType = AlarmChannelType.LOG;

        /**
         * 企业微信机器人 Webhook URL
         */
        private String wechatWebhook;

        /**
         * 钉钉机器人 Webhook URL
         */
        private String dingTalkWebhook;

        /**
         * 是否 @ 所有人
         */
        private Boolean mentionedAll = false;

        /**
         * @ 的手机号列表
         */
        private java.util.List<String> mentionedMobileList;
    }

    /**
     * 告警通知渠道类型
     */
    public enum AlarmChannelType {
        /**
         * 仅日志
         */
        LOG,

        /**
         * 企业微信
         */
        WECHAT,

        /**
         * 钉钉
         */
        DINGTALK,

        /**
         * 所有渠道
         */
        ALL
    }

    /**
     * 队列类型枚举
     */
    public enum QueueType {
        /**
         * 链表阻塞队列（标准，不支持动态调整容量）
         */
        LINKED_BLOCKING_QUEUE,

        /**
         * 可调整容量的链表阻塞队列（支持动态调整容量）
         */
        RESIZABLE_LINKED_BLOCKING_QUEUE,

        /**
         * 数组阻塞队列（不支持动态调整容量）
         */
        ARRAY_BLOCKING_QUEUE,

        /**
         * 同步队列（无容量概念）
         */
        SYNCHRONOUS_QUEUE,

        /**
         * 优先级队列（不支持动态调整容量）
         */
        PRIORITY_BLOCKING_QUEUE
    }

    /**
     * 拒绝策略类型
     */
    public enum RejectedPolicyType {
        /**
         * 抛出异常
         */
        ABORT_POLICY,

        /**
         * 调用者运行
         */
        CALLER_RUNS_POLICY,

        /**
         * 丢弃最老的任务
         */
        DISCARD_OLDEST_POLICY,

        /**
         * 丢弃当前任务
         */
        DISCARD_POLICY
    }
}
