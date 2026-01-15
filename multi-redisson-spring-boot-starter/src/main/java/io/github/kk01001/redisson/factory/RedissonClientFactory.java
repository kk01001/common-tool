package io.github.kk01001.redisson.factory;

import io.github.kk01001.redisson.properties.RedissonInstanceProperties;
import org.redisson.api.RedissonClient;

/**
 * @author kk01001
 * @date 2026-01-15 10:00:00
 * @description RedissonClient 工厂接口，根据配置创建客户端
 */
public interface RedissonClientFactory {

    /**
     * 创建 RedissonClient
     *
     * @param instanceName 实例名称
     * @param properties   实例配置
     * @return RedissonClient
     */
    RedissonClient create(String instanceName, RedissonInstanceProperties properties);
}
