package io.github.archer099.redisson.holder;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author archer099
 * @date 2026-01-15 10:00:00
 * @description RedissonClient 持有者，提供获取客户端的便捷方法
 */
public class RedissonClientHolder {

    private static final Logger log = LoggerFactory.getLogger(RedissonClientHolder.class);

    /**
     * 客户端映射
     */
    private final Map<String, RedissonClient> clients;

    /**
     * 主实例名称
     */
    private final String primaryName;

    public RedissonClientHolder(Map<String, RedissonClient> clients, String primaryName) {
        this.clients = new ConcurrentHashMap<>(clients);
        this.primaryName = primaryName;
        log.info("RedissonClientHolder initialized with {} clients, primary: {}",
                clients.size(), primaryName);
    }

    /**
     * 获取主 RedissonClient
     *
     * @return 主 RedissonClient
     */
    public RedissonClient getPrimary() {
        RedissonClient client = clients.get(primaryName);
        if (client == null) {
            throw new IllegalStateException("Primary RedissonClient not found: " + primaryName);
        }
        return client;
    }

    /**
     * 获取主实例名称
     *
     * @return 主实例名称
     */
    public String getPrimaryName() {
        return primaryName;
    }

    /**
     * 根据名称获取 RedissonClient
     *
     * @param name 实例名称
     * @return RedissonClient
     * @throws IllegalArgumentException 如果实例不存在
     */
    public RedissonClient getClient(String name) {
        RedissonClient client = clients.get(name);
        if (client == null) {
            throw new IllegalArgumentException("RedissonClient not found: " + name);
        }
        return client;
    }

    /**
     * 安全地根据名称获取 RedissonClient，不存在时返回 null
     *
     * @param name 实例名称
     * @return RedissonClient 或 null
     */
    public RedissonClient getClientOrNull(String name) {
        return clients.get(name);
    }

    /**
     * 检查是否存在指定名称的客户端
     *
     * @param name 实例名称
     * @return 是否存在
     */
    public boolean hasClient(String name) {
        return clients.containsKey(name);
    }

    /**
     * 获取所有客户端名称
     *
     * @return 客户端名称集合
     */
    public Set<String> getClientNames() {
        return Collections.unmodifiableSet(clients.keySet());
    }

    /**
     * 获取所有客户端
     *
     * @return 不可修改的客户端映射
     */
    public Map<String, RedissonClient> getAllClients() {
        return Collections.unmodifiableMap(clients);
    }

    /**
     * 获取客户端数量
     *
     * @return 客户端数量
     */
    public int size() {
        return clients.size();
    }

    /**
     * 注册新的客户端（运行时动态添加）
     *
     * @param name   实例名称
     * @param client RedissonClient
     */
    public void registerClient(String name, RedissonClient client) {
        if (clients.containsKey(name)) {
            log.warn("RedissonClient already exists: {}, will be replaced", name);
        }
        clients.put(name, client);
        log.info("RedissonClient registered: {}", name);
    }

    /**
     * 移除并关闭客户端
     *
     * @param name 实例名称
     * @return 是否成功移除
     */
    public boolean removeClient(String name) {
        if (primaryName.equals(name)) {
            log.warn("Cannot remove primary RedissonClient: {}", name);
            return false;
        }
        RedissonClient client = clients.remove(name);
        if (client != null) {
            try {
                client.shutdown();
                log.info("RedissonClient removed and shutdown: {}", name);
                return true;
            } catch (Exception e) {
                log.error("Failed to shutdown RedissonClient: {}", name, e);
            }
        }
        return false;
    }

    /**
     * 关闭所有客户端
     */
    public void shutdown() {
        log.info("Shutting down all RedissonClients...");
        for (Map.Entry<String, RedissonClient> entry : clients.entrySet()) {
            try {
                entry.getValue().shutdown();
                log.info("RedissonClient shutdown: {}", entry.getKey());
            } catch (Exception e) {
                log.error("Failed to shutdown RedissonClient: {}", entry.getKey(), e);
            }
        }
        clients.clear();
    }
}
