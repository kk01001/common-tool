package io.github.kk01001.idempotent.core;

import io.github.kk01001.idempotent.config.IdempotentProperties;
import io.github.kk01001.idempotent.exception.IdempotentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description Redis幂等执行器
 */
@Slf4j
@RequiredArgsConstructor
public class RedisIdempotentExecutor {

    private final RedissonClient redissonClient;
    private final IdempotentProperties properties;

    /**
     * 执行幂等性检查
     *
     * @param key 幂等key
     * @param expireSeconds 过期时间(秒)
     */
    public void execute(String key, long expireSeconds) {
        if (Boolean.TRUE.equals(properties.getDebugLog())) {
            log.debug("幂等性检查 - key: {}, expireSeconds: {}", key, expireSeconds);
        }

        RBucket<Object> bucket = redissonClient.getBucket(key);
        Duration duration = expireSeconds > 0 ? Duration.ofSeconds(expireSeconds) : properties.getDefaultExpireSeconds();
        boolean success = bucket.setIfAbsent(System.currentTimeMillis(), duration);

        if (!success) {
            throw new IdempotentException("重复请求");
        }
    }
} 