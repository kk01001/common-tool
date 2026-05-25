package io.github.archer099.redisson.factory;

import io.github.archer099.redisson.enums.DelayStrategyType;
import io.github.archer099.redisson.enums.FailedNodeDetectorType;
import io.github.archer099.redisson.enums.RedissonMode;
import io.github.archer099.redisson.enums.RedissonReadMode;
import io.github.archer099.redisson.properties.ClusterServerProperties;
import io.github.archer099.redisson.properties.FailedNodeDetectorProperties;
import io.github.archer099.redisson.properties.MasterSlaveServerProperties;
import io.github.archer099.redisson.properties.RedissonInstanceProperties;
import io.github.archer099.redisson.properties.RetryDelayProperties;
import io.github.archer099.redisson.properties.SentinelServerProperties;
import io.github.archer099.redisson.properties.SingleServerProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.FailedCommandsDetector;
import org.redisson.client.FailedCommandsTimeoutDetector;
import org.redisson.client.FailedConnectionDetector;
import org.redisson.client.FailedNodeDetector;
import org.redisson.client.codec.Codec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.ConstantDelay;
import org.redisson.config.DecorrelatedJitterDelay;
import org.redisson.config.DelayStrategy;
import org.redisson.config.EqualJitterDelay;
import org.redisson.config.FullJitterDelay;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.config.ReadMode;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.redisson.config.SubscriptionMode;
import org.redisson.config.TransportMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * @author archer099
 * @date 2026-01-15 10:00:00
 * @description RedissonClient 工厂实现类
 */
public class RedissonClientFactoryImpl implements RedissonClientFactory {

    private static final Logger log = LoggerFactory.getLogger(RedissonClientFactoryImpl.class);

    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";
    private static final String LINUX = "Linux";

    @Override
    public RedissonClient create(String instanceName, RedissonInstanceProperties properties) {
        log.info("Creating RedissonClient for instance: {}, mode: {}", instanceName, properties.getMode());

        Config config = new Config();

        // 配置通用参数
        configureCommon(config, properties);

        // 根据模式配置具体服务器
        RedissonMode mode = properties.getMode();
        switch (mode) {
            case SINGLE -> configureSingleServer(config, properties.getSingle());
            case SENTINEL -> configureSentinelServers(config, properties.getSentinel());
            case MASTER_SLAVE -> configureMasterSlaveServers(config, properties.getMasterSlave());
            case CLUSTER -> configureClusterServers(config, properties.getCluster());
            default -> throw new IllegalArgumentException("Unsupported Redisson mode: " + mode);
        }

        RedissonClient client = Redisson.create(config);
        log.info("RedissonClient created successfully for instance: {}", instanceName);

        return client;
    }

    /**
     * 配置通用参数
     */
    private void configureCommon(Config config, RedissonInstanceProperties properties) {
        config.setThreads(properties.getThreads());
        config.setNettyThreads(properties.getNettyThreads());
        config.setCheckLockSyncedSlaves(properties.isCheckLockSyncedSlaves());
        config.setSlavesSyncTimeout(properties.getSlavesSyncTimeout());
        config.setLockWatchdogTimeout(properties.getLockWatchdogTimeout());
        config.setKeepPubSubOrder(properties.isKeepPubSubOrder());
        config.setUseScriptCache(properties.isUseScriptCache());
        config.setMinCleanUpDelay(properties.getMinCleanUpDelay());
        config.setMaxCleanUpDelay(properties.getMaxCleanUpDelay());
        config.setCleanUpKeysAmount(properties.getCleanUpKeysAmount());
        config.setReliableTopicWatchdogTimeout(properties.getReliableTopicWatchdogTimeout());

        // 配置编解码器
        if (StringUtils.hasText(properties.getCodec())) {
            try {
                Class<?> codecClass = Class.forName(properties.getCodec());
                Codec codec = (Codec) codecClass.getDeclaredConstructor().newInstance();
                config.setCodec(codec);
            } catch (Exception e) {
                log.warn("Failed to create codec: {}, using default StringCodec", properties.getCodec(), e);
            }
        }

        // 配置传输模式
        configureTransportMode(config, properties.getTransportMode());
    }

    /**
     * 配置传输模式
     */
    private void configureTransportMode(Config config, String transportMode) {
        String osName = System.getProperty("os.name");
        boolean isLinux = StringUtils.hasLength(osName) && osName.contains(LINUX);

        if ("EPOLL".equalsIgnoreCase(transportMode) && isLinux) {
            log.info("Using EPOLL transport mode");
            config.setTransportMode(TransportMode.EPOLL);
        } else if ("KQUEUE".equalsIgnoreCase(transportMode)) {
            log.info("Using KQUEUE transport mode");
            config.setTransportMode(TransportMode.KQUEUE);
        } else {
            // 默认自动选择：Linux 使用 EPOLL，其他使用 NIO
            if ("AUTO".equalsIgnoreCase(transportMode) && isLinux) {
                log.info("Auto selecting EPOLL transport mode for Linux");
                config.setTransportMode(TransportMode.EPOLL);
            } else {
                log.info("Using NIO transport mode");
                config.setTransportMode(TransportMode.NIO);
            }
        }
    }

    /**
     * 配置单机模式
     */
    private void configureSingleServer(Config config, SingleServerProperties props) {
        if (props == null) {
            throw new IllegalArgumentException("Single server properties is required for SINGLE mode");
        }

        SingleServerConfig serverConfig = config.useSingleServer();

        // 地址
        serverConfig.setAddress(formatAddress(props.getAddress()));

        // 认证
        if (StringUtils.hasText(props.getPassword())) {
            serverConfig.setPassword(props.getPassword());
        }
        if (StringUtils.hasText(props.getUsername())) {
            serverConfig.setUsername(props.getUsername());
        }

        // 数据库
        serverConfig.setDatabase(props.getDatabase());

        // 连接池
        serverConfig.setConnectionPoolSize(props.getConnectionPoolSize());
        serverConfig.setConnectionMinimumIdleSize(props.getConnectionMinimumIdleSize());
        serverConfig.setSubscriptionConnectionPoolSize(props.getSubscriptionConnectionPoolSize());
        serverConfig.setSubscriptionConnectionMinimumIdleSize(props.getSubscriptionConnectionMinimumIdleSize());

        // 超时配置
        serverConfig.setConnectTimeout(props.getConnectTimeout());
        serverConfig.setTimeout(props.getTimeout());
        serverConfig.setIdleConnectionTimeout(props.getIdleConnectionTimeout());

        // 重试配置
        serverConfig.setRetryAttempts(props.getRetryAttempts());
        serverConfig.setRetryDelay(createDelayStrategy(props.getRetryDelay()));

        // DNS 监控
        serverConfig.setDnsMonitoringInterval(props.getDnsMonitoringInterval());

        // 客户端名称
        if (StringUtils.hasText(props.getClientName())) {
            serverConfig.setClientName(props.getClientName());
        }

        // 订阅配置
        serverConfig.setSubscriptionsPerConnection(props.getSubscriptionsPerConnection());

        // Ping 间隔
        serverConfig.setPingConnectionInterval(props.getPingConnectionInterval());

        // TCP 配置
        serverConfig.setKeepAlive(props.isKeepAlive());
        serverConfig.setTcpNoDelay(props.isTcpNoDelay());

        log.info("Configured single server: {}", props.getAddress());
    }

    /**
     * 配置哨兵模式
     */
    private void configureSentinelServers(Config config, SentinelServerProperties props) {
        if (props == null || props.getSentinelAddresses() == null || props.getSentinelAddresses().isEmpty()) {
            throw new IllegalArgumentException("Sentinel server properties and addresses are required for SENTINEL mode");
        }

        SentinelServersConfig serverConfig = config.useSentinelServers();

        // Master 名称
        serverConfig.setMasterName(props.getMasterName());

        // 哨兵地址
        for (String address : props.getSentinelAddresses()) {
            serverConfig.addSentinelAddress(formatAddress(address));
        }

        // 认证
        if (StringUtils.hasText(props.getPassword())) {
            serverConfig.setPassword(props.getPassword());
        }
        if (StringUtils.hasText(props.getUsername())) {
            serverConfig.setUsername(props.getUsername());
        }
        if (StringUtils.hasText(props.getSentinelPassword())) {
            serverConfig.setSentinelPassword(props.getSentinelPassword());
        }
        if (StringUtils.hasText(props.getSentinelUsername())) {
            serverConfig.setSentinelUsername(props.getSentinelUsername());
        }

        // 数据库
        serverConfig.setDatabase(props.getDatabase());

        // 读取模式
        serverConfig.setReadMode(ReadMode.valueOf(props.getReadMode().name()));
        serverConfig.setSubscriptionMode(SubscriptionMode.valueOf(props.getSubscriptionMode()));

        // 连接池
        serverConfig.setMasterConnectionPoolSize(props.getMasterConnectionPoolSize());
        serverConfig.setMasterConnectionMinimumIdleSize(props.getMasterConnectionMinimumIdleSize());
        serverConfig.setSlaveConnectionPoolSize(props.getSlaveConnectionPoolSize());
        serverConfig.setSlaveConnectionMinimumIdleSize(props.getSlaveConnectionMinimumIdleSize());
        serverConfig.setSubscriptionConnectionPoolSize(props.getSubscriptionConnectionPoolSize());
        serverConfig.setSubscriptionConnectionMinimumIdleSize(props.getSubscriptionConnectionMinimumIdleSize());

        // 超时配置
        serverConfig.setConnectTimeout(props.getConnectTimeout());
        serverConfig.setTimeout(props.getTimeout());
        serverConfig.setIdleConnectionTimeout(props.getIdleConnectionTimeout());

        // 重试配置
        serverConfig.setRetryAttempts(props.getRetryAttempts());
        serverConfig.setRetryDelay(createDelayStrategy(props.getRetryDelay()));

        // 故障检测
        serverConfig.setFailedSlaveReconnectionInterval(props.getFailedSlaveReconnectionInterval());
        serverConfig.setFailedSlaveNodeDetector(createFailedNodeDetector(props.getFailedNodeDetector()));

        // 扫描间隔
        serverConfig.setScanInterval(props.getScanInterval());
        serverConfig.setDnsMonitoringInterval(props.getDnsMonitoringInterval());

        // 客户端名称
        if (StringUtils.hasText(props.getClientName())) {
            serverConfig.setClientName(props.getClientName());
        }

        // 订阅配置
        serverConfig.setSubscriptionsPerConnection(props.getSubscriptionsPerConnection());

        // Ping 间隔
        serverConfig.setPingConnectionInterval(props.getPingConnectionInterval());

        // TCP 配置
        serverConfig.setKeepAlive(props.isKeepAlive());
        serverConfig.setTcpNoDelay(props.isTcpNoDelay());

        // 哨兵列表检查
        serverConfig.setCheckSentinelsList(props.isCheckSentinelsList());

        log.info("Configured sentinel servers, master: {}, sentinels: {}",
                props.getMasterName(), props.getSentinelAddresses());
    }

    /**
     * 配置主从模式
     */
    private void configureMasterSlaveServers(Config config, MasterSlaveServerProperties props) {
        if (props == null || !StringUtils.hasText(props.getMasterAddress())) {
            throw new IllegalArgumentException("Master-slave server properties and master address are required for MASTER_SLAVE mode");
        }

        MasterSlaveServersConfig serverConfig = config.useMasterSlaveServers();

        // 主节点地址
        serverConfig.setMasterAddress(formatAddress(props.getMasterAddress()));

        // 从节点地址
        if (props.getSlaveAddresses() != null && !props.getSlaveAddresses().isEmpty()) {
            for (String address : props.getSlaveAddresses()) {
                serverConfig.addSlaveAddress(formatAddress(address));
            }
        }

        // 认证
        if (StringUtils.hasText(props.getPassword())) {
            serverConfig.setPassword(props.getPassword());
        }
        if (StringUtils.hasText(props.getUsername())) {
            serverConfig.setUsername(props.getUsername());
        }

        // 数据库
        serverConfig.setDatabase(props.getDatabase());

        // 读取模式
        serverConfig.setReadMode(ReadMode.valueOf(props.getReadMode().name()));
        serverConfig.setSubscriptionMode(SubscriptionMode.valueOf(props.getSubscriptionMode()));

        // 连接池
        serverConfig.setMasterConnectionPoolSize(props.getMasterConnectionPoolSize());
        serverConfig.setMasterConnectionMinimumIdleSize(props.getMasterConnectionMinimumIdleSize());
        serverConfig.setSlaveConnectionPoolSize(props.getSlaveConnectionPoolSize());
        serverConfig.setSlaveConnectionMinimumIdleSize(props.getSlaveConnectionMinimumIdleSize());
        serverConfig.setSubscriptionConnectionPoolSize(props.getSubscriptionConnectionPoolSize());
        serverConfig.setSubscriptionConnectionMinimumIdleSize(props.getSubscriptionConnectionMinimumIdleSize());

        // 超时配置
        serverConfig.setConnectTimeout(props.getConnectTimeout());
        serverConfig.setTimeout(props.getTimeout());
        serverConfig.setIdleConnectionTimeout(props.getIdleConnectionTimeout());

        // 重试配置
        serverConfig.setRetryAttempts(props.getRetryAttempts());
        serverConfig.setRetryDelay(createDelayStrategy(props.getRetryDelay()));

        // 故障检测
        serverConfig.setFailedSlaveReconnectionInterval(props.getFailedSlaveReconnectionInterval());
        serverConfig.setFailedSlaveNodeDetector(createFailedNodeDetector(props.getFailedNodeDetector()));

        // DNS 监控
        serverConfig.setDnsMonitoringInterval(props.getDnsMonitoringInterval());

        // 客户端名称
        if (StringUtils.hasText(props.getClientName())) {
            serverConfig.setClientName(props.getClientName());
        }

        // 订阅配置
        serverConfig.setSubscriptionsPerConnection(props.getSubscriptionsPerConnection());

        // Ping 间隔
        serverConfig.setPingConnectionInterval(props.getPingConnectionInterval());

        // TCP 配置
        serverConfig.setKeepAlive(props.isKeepAlive());
        serverConfig.setTcpNoDelay(props.isTcpNoDelay());

        log.info("Configured master-slave servers, master: {}, slaves: {}",
                props.getMasterAddress(), props.getSlaveAddresses());
    }

    /**
     * 配置集群模式
     */
    private void configureClusterServers(Config config, ClusterServerProperties props) {
        if (props == null || props.getNodeAddresses() == null || props.getNodeAddresses().isEmpty()) {
            throw new IllegalArgumentException("Cluster server properties and node addresses are required for CLUSTER mode");
        }

        ClusterServersConfig serverConfig = config.useClusterServers();

        // 节点地址
        for (String address : props.getNodeAddresses()) {
            serverConfig.addNodeAddress(formatAddress(address));
        }

        // 认证
        if (StringUtils.hasText(props.getPassword())) {
            serverConfig.setPassword(props.getPassword());
        }
        if (StringUtils.hasText(props.getUsername())) {
            serverConfig.setUsername(props.getUsername());
        }

        // 扫描间隔
        serverConfig.setScanInterval(props.getScanInterval());

        // 读取模式
        serverConfig.setReadMode(ReadMode.valueOf(props.getReadMode().name()));
        serverConfig.setSubscriptionMode(SubscriptionMode.valueOf(props.getSubscriptionMode()));

        // 连接池
        serverConfig.setMasterConnectionPoolSize(props.getMasterConnectionPoolSize());
        serverConfig.setMasterConnectionMinimumIdleSize(props.getMasterConnectionMinimumIdleSize());
        serverConfig.setSlaveConnectionPoolSize(props.getSlaveConnectionPoolSize());
        serverConfig.setSlaveConnectionMinimumIdleSize(props.getSlaveConnectionMinimumIdleSize());
        serverConfig.setSubscriptionConnectionPoolSize(props.getSubscriptionConnectionPoolSize());
        serverConfig.setSubscriptionConnectionMinimumIdleSize(props.getSubscriptionConnectionMinimumIdleSize());

        // 超时配置
        serverConfig.setConnectTimeout(props.getConnectTimeout());
        serverConfig.setTimeout(props.getTimeout());
        serverConfig.setIdleConnectionTimeout(props.getIdleConnectionTimeout());

        // 重试配置
        serverConfig.setRetryAttempts(props.getRetryAttempts());
        serverConfig.setRetryDelay(createDelayStrategy(props.getRetryDelay()));

        // 槽位覆盖检查
        serverConfig.setCheckSlotsCoverage(props.isCheckSlotsCoverage());

        // 故障检测
        serverConfig.setFailedSlaveReconnectionInterval(props.getFailedSlaveReconnectionInterval());
        serverConfig.setFailedSlaveNodeDetector(createFailedNodeDetector(props.getFailedNodeDetector()));

        // 客户端名称
        if (StringUtils.hasText(props.getClientName())) {
            serverConfig.setClientName(props.getClientName());
        }

        // 订阅配置
        serverConfig.setSubscriptionsPerConnection(props.getSubscriptionsPerConnection());

        // Ping 间隔
        serverConfig.setPingConnectionInterval(props.getPingConnectionInterval());

        // TCP 配置
        serverConfig.setKeepAlive(props.isKeepAlive());
        serverConfig.setTcpNoDelay(props.isTcpNoDelay());

        log.info("Configured cluster servers, nodes: {}", props.getNodeAddresses());
    }

    /**
     * 创建故障节点检测器
     */
    private FailedNodeDetector createFailedNodeDetector(FailedNodeDetectorProperties props) {
        if (props == null) {
            return new FailedConnectionDetector();
        }

        FailedNodeDetectorType type = props.getType();
        long checkInterval = props.getCheckInterval();
        int failedCommandsLimit = props.getFailedCommandsLimit();

        return switch (type) {
            case CONNECTION -> new FailedConnectionDetector(checkInterval);
            case COMMANDS -> new FailedCommandsDetector(checkInterval, failedCommandsLimit);
            case COMMANDS_TIMEOUT -> new FailedCommandsTimeoutDetector(checkInterval, failedCommandsLimit);
        };
    }

    /**
     * 创建重试延迟策略
     */
    private DelayStrategy createDelayStrategy(RetryDelayProperties props) {
        if (props == null) {
            return new EqualJitterDelay(Duration.ofSeconds(1), Duration.ofSeconds(2));
        }

        DelayStrategyType type = props.getType();
        Duration minDelay = Duration.ofMillis(props.getMinDelay());
        Duration maxDelay = Duration.ofMillis(props.getMaxDelay());

        return switch (type) {
            case CONSTANT -> new ConstantDelay(minDelay);
            case EQUAL_JITTER -> new EqualJitterDelay(minDelay, maxDelay);
            case FULL_JITTER -> new FullJitterDelay(minDelay, maxDelay);
            case DECORRELATED_JITTER -> new DecorrelatedJitterDelay(minDelay, maxDelay);
        };
    }

    /**
     * 格式化地址，确保有 redis:// 前缀
     */
    private String formatAddress(String address) {
        if (address == null) {
            return null;
        }
        if (!address.startsWith(REDIS_PROTOCOL_PREFIX) && !address.startsWith(REDISS_PROTOCOL_PREFIX)) {
            return REDIS_PROTOCOL_PREFIX + address;
        }
        return address;
    }
}
