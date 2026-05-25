package io.github.archer099.redisson.properties;

import io.github.archer099.redisson.enums.RedissonReadMode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author archer099
 * @date 2026-01-15 10:00:00
 * @description 主从模式配置
 */
@Setter
@Getter
public class MasterSlaveServerProperties {

    /**
     * 主节点地址，格式：redis://host:port
     */
    private String masterAddress;

    /**
     * 从节点地址列表，格式：redis://host:port
     */
    private List<String> slaveAddresses;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户名（Redis 6.0+）
     */
    private String username;

    /**
     * 数据库索引
     */
    private int database = 0;

    /**
     * 读取模式：SLAVE, MASTER, MASTER_SLAVE
     */
    private RedissonReadMode readMode = RedissonReadMode.SLAVE;

    /**
     * 订阅模式：SLAVE, MASTER
     */
    private String subscriptionMode = "SLAVE";

    /**
     * Master 连接池大小
     */
    private int masterConnectionPoolSize = 64;

    /**
     * Master 最小空闲连接数
     */
    private int masterConnectionMinimumIdleSize = 24;

    /**
     * Slave 连接池大小
     */
    private int slaveConnectionPoolSize = 64;

    /**
     * Slave 最小空闲连接数
     */
    private int slaveConnectionMinimumIdleSize = 24;

    /**
     * 订阅连接池大小
     */
    private int subscriptionConnectionPoolSize = 50;

    /**
     * 最小订阅空闲连接数
     */
    private int subscriptionConnectionMinimumIdleSize = 1;

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 10000;

    /**
     * 命令等待超时时间（毫秒）
     */
    private int timeout = 3000;

    /**
     * 空闲连接超时时间（毫秒）
     */
    private int idleConnectionTimeout = 10000;

    /**
     * 重试次数
     */
    private int retryAttempts = 3;

    /**
     * 重试延迟策略配置
     */
    private RetryDelayProperties retryDelay = new RetryDelayProperties();

    /**
     * 故障从节点重连间隔（毫秒）
     */
    private int failedSlaveReconnectionInterval = 3000;

    /**
     * 故障节点检测器配置
     */
    private FailedNodeDetectorProperties failedNodeDetector = new FailedNodeDetectorProperties();

    /**
     * DNS 监控间隔（毫秒）
     */
    private int dnsMonitoringInterval = 5000;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 每个连接订阅数量限制
     */
    private int subscriptionsPerConnection = 5;

    /**
     * 负载均衡器类名
     */
    private String loadBalancer = "org.redisson.connection.balancer.RoundRobinLoadBalancer";

    /**
     * Ping 连接间隔（毫秒）
     */
    private int pingConnectionInterval = 30000;

    /**
     * 是否保持活跃
     */
    private boolean keepAlive = false;

    /**
     * 是否启用 TCP_NODELAY
     */
    private boolean tcpNoDelay = true;
}
