package io.github.kk01001.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.redis.core.MultiRedisClientManager;
import io.github.kk01001.redis.core.RedissonConfigFactory;
import io.github.kk01001.redis.util.RedissonUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 多 Redis 实例自动配置类，支持 cluster、主从、单机、sentinel 模式
 */
@Configuration
@ComponentScan(basePackages = "io.github.kk01001.redis")
@EnableConfigurationProperties(MultiRedisProperties.class)
public class MultiRedissonConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiRedissonConfiguration.class);

    /**
     * 创建多 Redis 客户端管理器
     *
     * @return Redis 客户端管理器
     */
    @Bean
    public MultiRedisClientManager multiRedisClientManager(MultiRedisProperties multiRedisProperties) {
        MultiRedisClientManager manager = new MultiRedisClientManager();

        // 设置默认实例名称
        manager.setDefaultInstanceName(multiRedisProperties.getDefaultInstance());
        manager.setBackupInstanceName(multiRedisProperties.getBackupInstance());
        manager.setMultiRedisProperties(multiRedisProperties);

        // 创建所有配置的 Redis 实例
        Map<String, MultiRedisProperties.RedisInstanceConfig> instances = multiRedisProperties.getInstances();

        if (instances.isEmpty()) {
            LOGGER.warn("未配置任何 Redis 实例");
            return manager;
        }

        for (Map.Entry<String, MultiRedisProperties.RedisInstanceConfig> entry : instances.entrySet()) {
            String instanceName = entry.getKey();
            MultiRedisProperties.RedisInstanceConfig instanceConfig = entry.getValue();

            if (!Boolean.TRUE.equals(instanceConfig.getEnabled())) {
                LOGGER.info("跳过未启用的 Redis 实例: {}", instanceName);
                continue;
            }

            try {
                RedissonClient redissonClient = createRedissonClient(instanceName, instanceConfig);
                manager.registerClient(instanceName, redissonClient);
                LOGGER.info("成功创建 Redis 实例: {}, 模式: {}", instanceName, instanceConfig.getMode());
            } catch (Exception e) {
                LOGGER.error("创建 Redis 实例失败: {}", instanceName, e);
                throw new RuntimeException("创建 Redis 实例失败: " + instanceName, e);
            }
        }

        return manager;
    }

    /**
     * 创建主 RedissonClient Bean（兼容性）
     *
     * @param multiRedisClientManager Redis 客户端管理器
     * @return 主 RedissonClient
     */
    @Bean(name = "redissonClient")
    @Primary
    public RedissonClient redissonClient(MultiRedisClientManager multiRedisClientManager) {
        return multiRedisClientManager.getDefaultClient();
    }

    /**
     * 创建备用 RedissonClient Bean（兼容性）
     *
     * @param multiRedisClientManager Redis 客户端管理器
     * @return 备用 RedissonClient，如果不存在则返回 null
     */
    @Bean(name = "backupRedissonClient")
    public RedissonClient backupRedissonClient(MultiRedisClientManager multiRedisClientManager) {
        return multiRedisClientManager.getBackupClient();
    }

    @Bean
    public RedissonUtil redissonUtil(@Qualifier("redissonClient") RedissonClient redissonClient,
                                     @Qualifier("backupRedissonClient") RedissonClient backupRedissonClient,
                                     @Qualifier("backupRedisExecutor") ExecutorService backupRedisExecutor,
                                     ObjectMapper objectMapper,
                                     MultiRedisClientManager redisClientManager) {
        return new RedissonUtil(redissonClient, backupRedissonClient, backupRedisExecutor, objectMapper, redisClientManager);
    }

    /**
     * 创建 Redisson 客户端
     *
     * @param instanceName   实例名称
     * @param instanceConfig 实例配置
     * @return Redisson 客户端
     */
    private RedissonClient createRedissonClient(String instanceName, MultiRedisProperties.RedisInstanceConfig instanceConfig) {
        LOGGER.info("创建 Redis 实例: {}, 模式: {}", instanceName, instanceConfig.getMode());

        Config config = RedissonConfigFactory.createConfig(instanceConfig);

        return Redisson.create(config);
    }
}
