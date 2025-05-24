package io.github.kk01001.redis.core;

import io.github.kk01001.redis.config.MultiRedisProperties;
import lombok.Setter;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description Redis 客户端管理器，用于管理多个 Redis 实例
 */
@Setter
public class MultiRedisClientManager implements DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiRedisClientManager.class);

    /**
     * Redis 客户端映射
     */
    private final Map<String, RedissonClient> redissonClients = new ConcurrentHashMap<>();

    private MultiRedisProperties multiRedisProperties;

    /**
     * 默认实例名称MultiRedisClientManager
     */
    private String defaultInstanceName;

    /**
     * 备用实例名称MultiRedisClientManager
     */
    private String backupInstanceName;

    /**
     * 注册 Redis 客户端
     *
     * @param instanceName   实例名称
     * @param redissonClient Redis 客户端
     */
    public void registerClient(String instanceName, RedissonClient redissonClient) {
        redissonClients.put(instanceName, redissonClient);
        LOGGER.info("注册 Redis 实例: {}", instanceName);
    }

    /**
     * 获取 Redis 客户端
     *
     * @param instanceName 实例名称
     * @return Redis 客户端
     */
    public RedissonClient getClient(String instanceName) {
        RedissonClient client = redissonClients.get(instanceName);
        if (client == null) {
            throw new IllegalArgumentException("未找到 Redis 实例: " + instanceName);
        }
        return client;
    }

    /**
     * 获取默认 Redis 客户端
     *
     * @return 默认 Redis 客户端
     */
    public RedissonClient getDefaultClient() {
        if (defaultInstanceName == null) {
            throw new IllegalStateException("未设置默认 Redis 实例");
        }
        return getClient(defaultInstanceName);
    }

    public RedissonClient getBackupClient() {
        // 查找名为 "back" 或 "backup" 的实例作为备用客户端
        if (hasInstance(backupInstanceName)) {
            return getClient(backupInstanceName);
        }

        // 如果没有专门的备用实例，返回 null
        LOGGER.info("未找到备用 Redis 实例（back 或 backup），backupRedissonClient 将为 null");
        return null;
    }

    /**
     * 是否启用备用实例
     *
     * @return 是否启用
     */
    public Boolean enableBackup() {
        MultiRedisProperties.RedisInstanceConfig instanceConfig = multiRedisProperties.getInstances().get(backupInstanceName);
        if (Objects.isNull(instanceConfig)) {
            return false;
        }
        return Boolean.TRUE.equals(instanceConfig.getEnabled());
    }

    /**
     * 获取所有实例名称
     *
     * @return 实例名称集合
     */
    public Set<String> getInstanceNames() {
        return redissonClients.keySet();
    }

    /**
     * 检查实例是否存在
     *
     * @param instanceName 实例名称
     * @return 是否存在
     */
    public boolean hasInstance(String instanceName) {
        return redissonClients.containsKey(instanceName);
    }

    /**
     * 移除 Redis 客户端
     *
     * @param instanceName 实例名称
     */
    public void removeClient(String instanceName) {
        RedissonClient client = redissonClients.remove(instanceName);
        if (client != null) {
            client.shutdown();
            LOGGER.info("移除 Redis 实例: {}", instanceName);
        }
    }

    @Override
    public void destroy() throws Exception {
        LOGGER.info("关闭所有 Redis 客户端");
        for (Map.Entry<String, RedissonClient> entry : redissonClients.entrySet()) {
            try {
                entry.getValue().shutdown();
                LOGGER.info("关闭 Redis 实例: {}", entry.getKey());
            } catch (Exception e) {
                LOGGER.error("关闭 Redis 实例失败: {}", entry.getKey(), e);
            }
        }
        redissonClients.clear();
    }
} 