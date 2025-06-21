package io.github.kk01001.dynamic.mq.config;

import io.github.kk01001.dynamic.mq.enums.MqType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.kk01001.dynamic.mq.enums.ConsumeMode;
import io.github.kk01001.dynamic.mq.enums.MessageModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 动态MQ配置属性
 */
@Data
@ConfigurationProperties(prefix = "dynamic.mq")
public class DynamicMqProperties {
    
    /**
     * 是否启用动态MQ
     */
    private boolean enabled = true;
    
    /**
     * 默认MQ类型
     */
    private MqType type = MqType.ROCKETMQ;
    
    /**
     * 生产者配置
     */
    private ProducerConfig producer = new ProducerConfig();
    
    /**
     * 消费者配置
     */
    private ConsumerConfig consumer = new ConsumerConfig();
    
    /**
     * RocketMQ配置
     */
    private RocketMqConfig rocketmq = new RocketMqConfig();
    
    /**
     * RabbitMQ配置
     */
    private RabbitMqConfig rabbitmq = new RabbitMqConfig();
    
    /**
     * Kafka配置
     */
    private KafkaConfig kafka = new KafkaConfig();
    
    /**
     * Redis配置
     */
    private RedisConfig redis = new RedisConfig();
    
    @Data
    public static class ProducerConfig {
        /**
         * 发送超时时间（毫秒）
         */
        private long sendTimeout = 3000;
        
        /**
         * 最大重试次数
         */
        private int maxRetryTimes = 3;
        
        /**
         * 是否启用事务
         */
        private boolean transactionEnabled = false;
    }
    
    @Data
    public static class ConsumerConfig {
        /**
         * 消费者组
         */
        private String groupName = "default-group";

        /**
         * 消费模式：并发消费(CONCURRENTLY)或顺序消费(ORDERLY)
         */
        private ConsumeMode consumeMode = ConsumeMode.CONCURRENTLY;

        /**
         * 消息模型：集群消费(CLUSTERING)或广播消费(BROADCASTING)
         */
        private MessageModel messageModel = MessageModel.CLUSTERING;

        /**
         * 消费线程数
         */
        private int consumeThreadMin = 1;
        private int consumeThreadMax = 10;

        /**
         * 批量消费大小
         */
        private int consumeMessageBatchMaxSize = 1;

        /**
         * 重试策略配置
         */
        private RetryConfig retry = new RetryConfig();

        /**
         * 死信队列配置
         */
        private DeadLetterConfig deadLetter = new DeadLetterConfig();

        /**
         * 消息轨迹配置
         */
        private TraceConfig trace = new TraceConfig();

        /**
         * 消息过滤配置
         */
        private FilterConfig filter = new FilterConfig();
    }
    
    @Data
    public static class RocketMqConfig {
        /**
         * NameServer地址
         */
        private String nameServer = "localhost:9876";
        
        /**
         * 生产者组
         */
        private String producerGroup = "default-producer-group";
        
        /**
         * 其他配置
         */
        private Map<String, Object> properties = new HashMap<>();
    }
    
    @Data
    public static class RabbitMqConfig {
        /**
         * 连接地址
         */
        private String host = "localhost";
        
        /**
         * 端口
         */
        private int port = 5672;
        
        /**
         * 用户名
         */
        private String username = "guest";
        
        /**
         * 密码
         */
        private String password = "guest";
        
        /**
         * 虚拟主机
         */
        private String virtualHost = "/";
        
        /**
         * 其他配置
         */
        private Map<String, Object> properties = new HashMap<>();
    }
    
    @Data
    public static class KafkaConfig {
        /**
         * Bootstrap服务器
         */
        private String bootstrapServers = "localhost:9092";
        
        /**
         * 其他配置
         */
        private Map<String, Object> properties = new HashMap<>();
    }
    
    @Data
    public static class RedisConfig {
        /**
         * 是否使用Redisson（默认true）
         */
        private boolean useRedisson = true;

        /**
         * Redis主机
         */
        private String host = "localhost";

        /**
         * Redis端口
         */
        private int port = 6379;

        /**
         * 密码
         */
        private String password;

        /**
         * 数据库索引
         */
        private int database = 0;

        /**
         * Redisson配置
         */
        private RedissonConfig redisson = new RedissonConfig();

        /**
         * 其他配置
         */
        private Map<String, Object> properties = new HashMap<>();
    }

    @Data
    public static class RedissonConfig {
        /**
         * 连接模式：single, cluster, sentinel, master-slave
         */
        private String mode = "single";

        /**
         * 单机模式配置
         */
        private SingleServerConfig singleServer = new SingleServerConfig();

        /**
         * 集群模式配置
         */
        private ClusterServersConfig clusterServers = new ClusterServersConfig();

        /**
         * 哨兵模式配置
         */
        private SentinelServersConfig sentinelServers = new SentinelServersConfig();

        /**
         * 连接池大小
         */
        private int connectionPoolSize = 64;

        /**
         * 最小空闲连接数
         */
        private int connectionMinimumIdleSize = 24;

        /**
         * 连接超时时间（毫秒）
         */
        private int connectTimeout = 10000;

        /**
         * 命令等待超时时间（毫秒）
         */
        private int timeout = 3000;

        /**
         * 重试次数
         */
        private int retryAttempts = 3;

        /**
         * 重试间隔（毫秒）
         */
        private int retryInterval = 1500;
    }

    @Data
    public static class SingleServerConfig {
        /**
         * 服务器地址
         */
        private String address = "redis://localhost:6379";

        /**
         * 连接池大小
         */
        private int connectionPoolSize = 64;

        /**
         * 最小空闲连接数
         */
        private int connectionMinimumIdleSize = 24;
    }

    @Data
    public static class ClusterServersConfig {
        /**
         * 集群节点地址
         */
        private List<String> nodeAddresses = new ArrayList<>();

        /**
         * 主节点连接池大小
         */
        private int masterConnectionPoolSize = 64;

        /**
         * 从节点连接池大小
         */
        private int slaveConnectionPoolSize = 64;
    }

    @Data
    public static class SentinelServersConfig {
        /**
         * 主服务器名称
         */
        private String masterName = "mymaster";

        /**
         * 哨兵地址
         */
        private List<String> sentinelAddresses = new ArrayList<>();

        /**
         * 主节点连接池大小
         */
        private int masterConnectionPoolSize = 64;

        /**
         * 从节点连接池大小
         */
        private int slaveConnectionPoolSize = 64;
    }

    @Data
    public static class RetryConfig {
        /**
         * 是否启用重试
         */
        private boolean enabled = true;

        /**
         * 最大重试次数
         */
        private int maxRetryTimes = 3;

        /**
         * 重试策略类型：fixed, exponential, custom
         */
        private String strategy = "fixed";

        /**
         * 固定延迟时间（毫秒）
         */
        private long fixedDelay = 1000L;

        /**
         * 指数退避初始延迟（毫秒）
         */
        private long initialDelay = 1000L;

        /**
         * 指数退避倍数
         */
        private double multiplier = 2.0;
    }

    @Data
    public static class DeadLetterConfig {
        /**
         * 是否启用死信队列
         */
        private boolean enabled = true;

        /**
         * 死信队列处理器类型：default, logging, custom
         */
        private String handler = "default";
    }

    @Data
    public static class TraceConfig {
        /**
         * 是否启用消息轨迹
         */
        private boolean enabled = false;

        /**
         * 轨迹记录器类型：default, logging, custom
         */
        private String tracer = "default";
    }

    @Data
    public static class FilterConfig {
        /**
         * 是否启用消息过滤
         */
        private boolean enabled = false;

        /**
         * 默认过滤类型：tag, sql, custom
         */
        private String defaultType = "tag";
    }
}
