package io.github.kk01001.redis.core;

import io.github.kk01001.redis.config.MultiRedisProperties;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description Redis 配置工厂类，用于根据不同模式创建 Redisson 配置
 */
public class RedissonConfigFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonConfigFactory.class);

    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";
    private static final String LINUX = "Linux";

    /**
     * 创建 Redisson 配置
     *
     * @param instanceConfig Redis 实例配置
     * @return Redisson 配置
     */
    public static Config createConfig(MultiRedisProperties.RedisInstanceConfig instanceConfig) {
        Config config = new Config();

        // 设置编码器
        config.setCodec(getCodec(instanceConfig.getCodecClass()));

        // 设置传输模式
        setTransportMode(config);

        // 设置 Netty 线程数
        config.setNettyThreads(instanceConfig.getNettyThreads());

        // 设置锁同步配置
        config.setCheckLockSyncedSlaves(instanceConfig.isCheckLockSyncedSlaves());
        config.setSlavesSyncTimeout(instanceConfig.getSlavesSyncTimeout());

        // 根据模式配置不同的 Redis 连接
        switch (instanceConfig.getMode()) {
            case SINGLE:
                configureSingle(config, instanceConfig);
                break;
            case MASTER_SLAVE:
                configureMasterSlave(config, instanceConfig);
                break;
            case CLUSTER:
                configureCluster(config, instanceConfig);
                break;
            case SENTINEL:
                configureSentinel(config, instanceConfig);
                break;
            default:
                throw new IllegalArgumentException("不支持的 Redis 模式: " + instanceConfig.getMode());
        }

        return config;
    }

    private static Codec getCodec(String codecClass) {
        if (StringUtils.hasLength(codecClass)) {
            try {
                return (Codec) Class.forName(codecClass).newInstance();
            } catch (Exception e) {
                LOGGER.error("无法创建编码器: {}", codecClass, e);
            }
        }
        return StringCodec.INSTANCE;
    }

    /**
     * 设置传输模式
     */
    private static void setTransportMode(Config config) {
        String osName = System.getProperty("os.name");
        if (StringUtils.hasLength(osName) && osName.contains(LINUX)) {
            LOGGER.info("Redisson 使用 EPOLL 传输模式");
            config.setTransportMode(TransportMode.EPOLL);
        }
    }

    /**
     * 配置单机模式
     */
    private static void configureSingle(Config config, MultiRedisProperties.RedisInstanceConfig instanceConfig) {
        MultiRedisProperties.SingleConfig singleConfig = instanceConfig.getSingle();

        if (!StringUtils.hasLength(singleConfig.getAddress())) {
            throw new IllegalArgumentException("单机模式必须配置 address");
        }

        String address = formatAddress(singleConfig.getAddress());

        config.useSingleServer()
                .setAddress(address)
                .setDatabase(instanceConfig.getDatabase())
                .setPassword(instanceConfig.getPassword())
                .setConnectTimeout(instanceConfig.getConnectionTimeout())
                .setTimeout(instanceConfig.getResponseTimeout())
                .setIdleConnectionTimeout(instanceConfig.getIdleConnectionTimeout())
                .setRetryAttempts(instanceConfig.getRetryAttempts())
                .setRetryInterval(instanceConfig.getRetryInterval())
                .setConnectionPoolSize(singleConfig.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(singleConfig.getConnectionMinimumIdleSize());
    }

    /**
     * 配置主从模式
     */
    private static void configureMasterSlave(Config config, MultiRedisProperties.RedisInstanceConfig instanceConfig) {
        MultiRedisProperties.MasterSlaveConfig masterSlaveConfig = instanceConfig.getMasterSlave();

        if (!StringUtils.hasLength(masterSlaveConfig.getMasterAddress())) {
            throw new IllegalArgumentException("主从模式必须配置 masterAddress");
        }

        String masterAddress = formatAddress(masterSlaveConfig.getMasterAddress());

        config.useMasterSlaveServers()
                .setMasterAddress(masterAddress)
                .setDatabase(instanceConfig.getDatabase())
                .setPassword(instanceConfig.getPassword())
                .setConnectTimeout(instanceConfig.getConnectionTimeout())
                .setTimeout(instanceConfig.getResponseTimeout())
                .setIdleConnectionTimeout(instanceConfig.getIdleConnectionTimeout())
                .setRetryAttempts(instanceConfig.getRetryAttempts())
                .setRetryInterval(instanceConfig.getRetryInterval())
                .setMasterConnectionPoolSize(masterSlaveConfig.getMasterConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(masterSlaveConfig.getMasterConnectionMinimumIdleSize())
                .setSlaveConnectionPoolSize(masterSlaveConfig.getSlaveConnectionPoolSize())
                .setSlaveConnectionMinimumIdleSize(masterSlaveConfig.getSlaveConnectionMinimumIdleSize())
                .setReadMode(masterSlaveConfig.getReadMode());

        // 添加从节点地址
        if (masterSlaveConfig.getSlaveAddresses() != null && !masterSlaveConfig.getSlaveAddresses().isEmpty()) {
            String[] slaveAddresses = formatAddresses(masterSlaveConfig.getSlaveAddresses());
            config.useMasterSlaveServers().addSlaveAddress(slaveAddresses);
        }
    }

    /**
     * 配置集群模式
     */
    private static void configureCluster(Config config, MultiRedisProperties.RedisInstanceConfig instanceConfig) {
        MultiRedisProperties.ClusterConfig clusterConfig = instanceConfig.getCluster();

        if (clusterConfig.getNodeAddresses() == null || clusterConfig.getNodeAddresses().isEmpty()) {
            throw new IllegalArgumentException("集群模式必须配置 nodeAddresses");
        }

        String[] nodeAddresses = formatAddresses(clusterConfig.getNodeAddresses());

        config.useClusterServers()
                .addNodeAddress(nodeAddresses)
                .setPassword(instanceConfig.getPassword())
                .setConnectTimeout(instanceConfig.getConnectionTimeout())
                .setTimeout(instanceConfig.getResponseTimeout())
                .setIdleConnectionTimeout(instanceConfig.getIdleConnectionTimeout())
                .setRetryAttempts(instanceConfig.getRetryAttempts())
                .setRetryInterval(instanceConfig.getRetryInterval())
                .setMasterConnectionPoolSize(clusterConfig.getMasterConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(clusterConfig.getMasterConnectionMinimumIdleSize())
                .setSlaveConnectionPoolSize(clusterConfig.getSlaveConnectionPoolSize())
                .setSlaveConnectionMinimumIdleSize(clusterConfig.getSlaveConnectionMinimumIdleSize())
                .setReadMode(clusterConfig.getReadMode())
                .setScanInterval(clusterConfig.getScanInterval())
                .setCheckSlotsCoverage(clusterConfig.isCheckSlotsCoverage());
    }

    /**
     * 配置哨兵模式
     */
    private static void configureSentinel(Config config, MultiRedisProperties.RedisInstanceConfig instanceConfig) {
        MultiRedisProperties.SentinelConfig sentinelConfig = instanceConfig.getSentinel();

        if (!StringUtils.hasLength(sentinelConfig.getMasterName())) {
            throw new IllegalArgumentException("哨兵模式必须配置 masterName");
        }

        if (sentinelConfig.getSentinelAddresses() == null || sentinelConfig.getSentinelAddresses().isEmpty()) {
            throw new IllegalArgumentException("哨兵模式必须配置 sentinelAddresses");
        }

        String[] sentinelAddresses = formatAddresses(sentinelConfig.getSentinelAddresses());

        config.useSentinelServers()
                .setMasterName(sentinelConfig.getMasterName())
                .addSentinelAddress(sentinelAddresses)
                .setDatabase(instanceConfig.getDatabase())
                .setPassword(instanceConfig.getPassword())
                .setConnectTimeout(instanceConfig.getConnectionTimeout())
                .setTimeout(instanceConfig.getResponseTimeout())
                .setIdleConnectionTimeout(instanceConfig.getIdleConnectionTimeout())
                .setRetryAttempts(instanceConfig.getRetryAttempts())
                .setRetryInterval(instanceConfig.getRetryInterval())
                .setMasterConnectionPoolSize(sentinelConfig.getMasterConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(sentinelConfig.getMasterConnectionMinimumIdleSize())
                .setSlaveConnectionPoolSize(sentinelConfig.getSlaveConnectionPoolSize())
                .setSlaveConnectionMinimumIdleSize(sentinelConfig.getSlaveConnectionMinimumIdleSize())
                .setReadMode(sentinelConfig.getReadMode())
                .setScanInterval(sentinelConfig.getScanInterval());
    }

    /**
     * 格式化单个地址
     */
    private static String formatAddress(String address) {
        if (!address.startsWith(REDIS_PROTOCOL_PREFIX) && !address.startsWith(REDISS_PROTOCOL_PREFIX)) {
            return REDIS_PROTOCOL_PREFIX + address;
        }
        return address;
    }

    /**
     * 格式化地址列表
     */
    private static String[] formatAddresses(List<String> addresses) {
        List<String> formattedAddresses = new ArrayList<>(addresses.size());
        for (String address : addresses) {
            formattedAddresses.add(formatAddress(address));
        }
        return formattedAddresses.toArray(new String[0]);
    }
} 