package io.github.kk01001.redis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description RedisTemplate 自动配置类，基于默认实例创建 RedisTemplate 和连接工厂
 */
@Configuration
@EnableConfigurationProperties(MultiRedisProperties.class)
public class RedisTemplateAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisTemplateAutoConfiguration.class);

    private final MultiRedisProperties multiRedisProperties;

    public RedisTemplateAutoConfiguration(MultiRedisProperties multiRedisProperties) {
        this.multiRedisProperties = multiRedisProperties;
    }

    /**
     * 创建默认的 RedisConnectionFactory Bean
     *
     * @return Redis 连接工厂
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        String defaultInstanceName = multiRedisProperties.getDefaultInstance();
        MultiRedisProperties.RedisInstanceConfig defaultConfig = multiRedisProperties.getInstances().get(defaultInstanceName);

        if (defaultConfig == null) {
            throw new IllegalArgumentException("未找到默认 Redis 实例配置: " + defaultInstanceName);
        }

        LOGGER.info("创建 RedisConnectionFactory，使用实例: {}, 模式: {}", defaultInstanceName, defaultConfig.getMode());
        return createRedisConnectionFactory(defaultConfig);
    }

    /**
     * 创建默认的 RedisTemplate Bean
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return RedisTemplate
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 设置序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        // key 和 hashKey 使用 String 序列化
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // value 和 hashValue 使用 String 序列化
        template.setValueSerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);

        template.afterPropertiesSet();

        LOGGER.info("创建默认 RedisTemplate，使用实例: {}", multiRedisProperties.getDefaultInstance());
        return template;
    }

    /**
     * 创建 Redis 连接工厂
     *
     * @param instanceConfig 实例配置
     * @return Redis 连接工厂
     */
    private RedisConnectionFactory createRedisConnectionFactory(MultiRedisProperties.RedisInstanceConfig instanceConfig) {
        LettuceConnectionFactory factory;

        switch (instanceConfig.getMode()) {
            case SINGLE:
                factory = createSingleConnectionFactory(instanceConfig);
                break;
            case MASTER_SLAVE:
                // 主从模式使用主节点地址创建单机连接
                factory = createMasterSlaveConnectionFactory(instanceConfig);
                break;
            case CLUSTER:
                factory = createClusterConnectionFactory(instanceConfig);
                break;
            case SENTINEL:
                factory = createSentinelConnectionFactory(instanceConfig);
                break;
            default:
                throw new IllegalArgumentException("不支持的 Redis 模式: " + instanceConfig.getMode());
        }

        // 设置连接超时
        factory.setTimeout(instanceConfig.getResponseTimeout());
        factory.setValidateConnection(true);

        return factory;
    }

    /**
     * 创建单机连接工厂
     *
     * @param instanceConfig 实例配置
     * @return Lettuce 连接工厂
     */
    private LettuceConnectionFactory createSingleConnectionFactory(MultiRedisProperties.RedisInstanceConfig instanceConfig) {
        MultiRedisProperties.SingleConfig singleConfig = instanceConfig.getSingle();
        String[] hostPort = singleConfig.getAddress().split(":");

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(hostPort[0]);
        config.setPort(Integer.parseInt(hostPort[1]));
        config.setDatabase(instanceConfig.getDatabase());

        if (instanceConfig.getPassword() != null && !instanceConfig.getPassword().isEmpty()) {
            config.setPassword(instanceConfig.getPassword());
        }

        LOGGER.debug("创建单机连接工厂: {}:{}, 数据库: {}", hostPort[0], hostPort[1], instanceConfig.getDatabase());
        return new LettuceConnectionFactory(config);
    }

    /**
     * 创建主从连接工厂
     *
     * @param instanceConfig 实例配置
     * @return Lettuce 连接工厂
     */
    private LettuceConnectionFactory createMasterSlaveConnectionFactory(MultiRedisProperties.RedisInstanceConfig instanceConfig) {
        MultiRedisProperties.MasterSlaveConfig masterSlaveConfig = instanceConfig.getMasterSlave();
        String[] hostPort = masterSlaveConfig.getMasterAddress().split(":");

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(hostPort[0]);
        config.setPort(Integer.parseInt(hostPort[1]));
        config.setDatabase(instanceConfig.getDatabase());

        if (instanceConfig.getPassword() != null && !instanceConfig.getPassword().isEmpty()) {
            config.setPassword(instanceConfig.getPassword());
        }

        LOGGER.debug("创建主从连接工厂（使用主节点）: {}:{}, 数据库: {}", hostPort[0], hostPort[1], instanceConfig.getDatabase());
        return new LettuceConnectionFactory(config);
    }

    /**
     * 创建集群连接工厂
     *
     * @param instanceConfig 实例配置
     * @return Lettuce 连接工厂
     */
    private LettuceConnectionFactory createClusterConnectionFactory(MultiRedisProperties.RedisInstanceConfig instanceConfig) {
        MultiRedisProperties.ClusterConfig clusterConfig = instanceConfig.getCluster();

        RedisClusterConfiguration config = new RedisClusterConfiguration();

        for (String nodeAddress : clusterConfig.getNodeAddresses()) {
            String[] hostPort = nodeAddress.split(":");
            config.clusterNode(hostPort[0], Integer.parseInt(hostPort[1]));
        }

        if (instanceConfig.getPassword() != null && !instanceConfig.getPassword().isEmpty()) {
            config.setPassword(instanceConfig.getPassword());
        }

        LOGGER.debug("创建集群连接工厂，节点数量: {}", clusterConfig.getNodeAddresses().size());
        return new LettuceConnectionFactory(config);
    }

    /**
     * 创建哨兵连接工厂
     *
     * @param instanceConfig 实例配置
     * @return Lettuce 连接工厂
     */
    private LettuceConnectionFactory createSentinelConnectionFactory(MultiRedisProperties.RedisInstanceConfig instanceConfig) {
        MultiRedisProperties.SentinelConfig sentinelConfig = instanceConfig.getSentinel();

        RedisSentinelConfiguration config = new RedisSentinelConfiguration();
        config.setMaster(sentinelConfig.getMasterName());
        config.setDatabase(instanceConfig.getDatabase());

        for (String sentinelAddress : sentinelConfig.getSentinelAddresses()) {
            String[] hostPort = sentinelAddress.split(":");
            config.sentinel(hostPort[0], Integer.parseInt(hostPort[1]));
        }

        if (instanceConfig.getPassword() != null && !instanceConfig.getPassword().isEmpty()) {
            config.setPassword(instanceConfig.getPassword());
        }

        LOGGER.debug("创建哨兵连接工厂，主服务器: {}, 哨兵数量: {}",
                sentinelConfig.getMasterName(), sentinelConfig.getSentinelAddresses().size());
        return new LettuceConnectionFactory(config);
    }
} 