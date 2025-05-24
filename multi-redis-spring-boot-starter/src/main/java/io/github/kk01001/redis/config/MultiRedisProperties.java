package io.github.kk01001.redis.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.redisson.config.ReadMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 多 Redis 实例配置属性类，支持 cluster、主从、单机、sentinel 模式
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ConfigurationProperties(prefix = "multi.redis")
public class MultiRedisProperties implements Serializable {

    /**
     * 是否启用多 Redis 配置
     */
    private boolean enabled = false;

    /**
     * 默认 Redis 实例名称
     */
    private String defaultInstance = "default";

    /**
     * 备份 Redis 实例名称
     */
    private String backupInstance = "backup";

    /**
     * Redis 实例配置映射
     */
    private Map<String, RedisInstanceConfig> instances = new HashMap<>();

    @Setter
    @Getter
    public static class RedisInstanceConfig implements Serializable {

        /**
         * Redis 模式：single、master-slave、cluster、sentinel
         */
        private RedisMode mode = RedisMode.SINGLE;

        /**
         * 是否启用此实例
         */
        private Boolean enabled = true;

        /**
         * Redis 密码
         */
        private String password;

        /**
         * 数据库索引（仅单机和主从模式有效）
         */
        private int database = 0;

        /**
         * 连接超时时间（毫秒）
         */
        private int connectionTimeout = 5000;

        /**
         * 响应超时时间（毫秒）
         */
        private int responseTimeout = 3000;

        /**
         * 空闲连接超时时间（毫秒）
         */
        private int idleConnectionTimeout = 10000;

        /**
         * 重试次数
         */
        private int retryAttempts = 3;

        /**
         * 重试间隔（毫秒）
         */
        private int retryInterval = 1000;

        /**
         * Netty 线程数
         */
        private int nettyThreads = 32;

        /**
         * 是否检查锁同步从节点
         */
        private boolean checkLockSyncedSlaves = false;

        /**
         * 从节点同步超时时间（毫秒）
         */
        private long slavesSyncTimeout = 1000;

        /**
         * 编码
         */
        private String codecClass = "org.redisson.client.codec.StringCodec";

        /**
         * 单机模式配置
         */
        private SingleConfig single = new SingleConfig();

        /**
         * 主从模式配置
         */
        private MasterSlaveConfig masterSlave = new MasterSlaveConfig();

        /**
         * 集群模式配置
         */
        private ClusterConfig cluster = new ClusterConfig();

        /**
         * 哨兵模式配置
         */
        private SentinelConfig sentinel = new SentinelConfig();

        /**
         * 连接池配置
         */
        private PoolConfig pool = new PoolConfig();
    }

    /**
     * Redis 模式枚举
     */
    public enum RedisMode {
        /**
         * 单机模式
         */
        SINGLE,
        /**
         * 主从模式
         */
        MASTER_SLAVE,
        /**
         * 集群模式
         */
        CLUSTER,
        /**
         * 哨兵模式
         */
        SENTINEL
    }

    @Setter
    @Getter
    public static class SingleConfig implements Serializable {

        /**
         * Redis 服务器地址 host:port
         */
        private String address;

        /**
         * 连接池大小
         */
        private int connectionPoolSize = 64;

        /**
         * 最小空闲连接数
         */
        private int connectionMinimumIdleSize = 24;
    }

    @Setter
    @Getter
    public static class MasterSlaveConfig implements Serializable {

        /**
         * 主节点地址 host:port
         */
        private String masterAddress;

        /**
         * 从节点地址列表 host:port
         */
        private List<String> slaveAddresses;

        /**
         * 主节点连接池大小
         */
        private int masterConnectionPoolSize = 64;

        /**
         * 主节点最小空闲连接数
         */
        private int masterConnectionMinimumIdleSize = 24;

        /**
         * 从节点连接池大小
         */
        private int slaveConnectionPoolSize = 64;

        /**
         * 从节点最小空闲连接数
         */
        private int slaveConnectionMinimumIdleSize = 24;

        /**
         * 读取模式
         */
        private ReadMode readMode = ReadMode.SLAVE;
    }

    @Setter
    @Getter
    public static class ClusterConfig implements Serializable {

        /**
         * 集群节点地址列表 host:port
         */
        private List<String> nodeAddresses;

        /**
         * 主节点连接池大小
         */
        private int masterConnectionPoolSize = 64;

        /**
         * 主节点最小空闲连接数
         */
        private int masterConnectionMinimumIdleSize = 24;

        /**
         * 从节点连接池大小
         */
        private int slaveConnectionPoolSize = 64;

        /**
         * 从节点最小空闲连接数
         */
        private int slaveConnectionMinimumIdleSize = 24;

        /**
         * 读取模式
         */
        private ReadMode readMode = ReadMode.SLAVE;

        /**
         * 集群扫描间隔（毫秒）
         */
        private int scanInterval = 5000;

        /**
         * 是否检查槽位覆盖
         */
        private boolean checkSlotsCoverage = true;

        /**
         * 最大重定向次数
         */
        private int maxRedirects = 3;
    }

    @Setter
    @Getter
    public static class SentinelConfig implements Serializable {

        /**
         * 主服务器名称
         */
        private String masterName;

        /**
         * 哨兵节点地址列表 host:port
         */
        private List<String> sentinelAddresses;

        /**
         * 主节点连接池大小
         */
        private int masterConnectionPoolSize = 64;

        /**
         * 主节点最小空闲连接数
         */
        private int masterConnectionMinimumIdleSize = 24;

        /**
         * 从节点连接池大小
         */
        private int slaveConnectionPoolSize = 64;

        /**
         * 从节点最小空闲连接数
         */
        private int slaveConnectionMinimumIdleSize = 24;

        /**
         * 读取模式
         */
        private ReadMode readMode = ReadMode.SLAVE;

        /**
         * 哨兵扫描间隔（毫秒）
         */
        private int scanInterval = 1000;
    }

    @Setter
    @Getter
    public static class PoolConfig implements Serializable {

        /**
         * 最大空闲连接数
         */
        private int maxIdle = 50;

        /**
         * 最小空闲连接数
         */
        private int minIdle = 10;

        /**
         * 最大活跃连接数
         */
        private int maxActive = 200;

        /**
         * 最大等待时间（毫秒）
         */
        private long maxWait = -1;

        /**
         * 逐出扫描的时间间隔（毫秒）
         */
        private long timeBetweenEvictionRuns = -1;
    }
}
