package io.github.archer099.redisson.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * @author archer099
 * @date 2026-01-15 10:00:00
 * @description 单机模式配置
 */
@Setter
@Getter
public class SingleServerProperties {

    /**
     * Redis 服务器地址，格式：redis://host:port 或 rediss://host:port (SSL)
     */
    private String address;

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
     * 连接池大小
     */
    private int connectionPoolSize = 64;

    /**
     * 最小空闲连接数
     */
    private int connectionMinimumIdleSize = 24;

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
     * DNS 监控间隔（毫秒），-1 表示禁用
     */
    private int dnsMonitoringInterval = 5000;

    /**
     * 是否启用 SSL 端点识别
     */
    private boolean sslEnableEndpointIdentification = true;

    /**
     * SSL 提供者：JDK, OPENSSL
     */
    private String sslProvider;

    /**
     * SSL 信任库路径
     */
    private String sslTruststore;

    /**
     * SSL 信任库密码
     */
    private String sslTruststorePassword;

    /**
     * SSL 密钥库路径
     */
    private String sslKeystore;

    /**
     * SSL 密钥库密码
     */
    private String sslKeystorePassword;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 每个连接订阅数量限制
     */
    private int subscriptionsPerConnection = 5;

    /**
     * 发布订阅连接池大小
     */
    private int pubSubConnectionPoolSize = 50;

    /**
     * 发布订阅连接最小空闲数
     */
    private int pubSubConnectionMinimumIdleSize = 1;

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
