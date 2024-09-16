package io.github.kk01001.lock.core.impl;

import io.github.kk01001.lock.core.LockStrategy;
import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.model.Rule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @author linshiqiang
 * @date 2024-09-16 12:20:00
 * @description 单机信号量限并发
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedissonSemaphoreLockStrategyImpl implements LockStrategy {

    private final RedissonClient redissonClient;

    @Override
    public LockType getType() {
        return LockType.REDISSON_SEMAPHORE;
    }

    @SneakyThrows
    @Override
    public void lock(Rule rule) {
        RSemaphore semaphore = getSemaphore(rule);
        semaphore.acquire();
    }

    @SneakyThrows
    @Override
    public boolean tryLock(Rule rule) {
        RSemaphore semaphore = getSemaphore(rule);
        return semaphore.tryAcquire(Duration.ofMillis(rule.getTimeout()));
    }

    @Override
    public void unlock(Rule rule) {
        RSemaphore semaphore = getSemaphore(rule);
        semaphore.release();
    }

    private RSemaphore getSemaphore(Rule rule) {
        String key = rule.getKey();
        RSemaphore semaphore = redissonClient.getSemaphore(key);
        semaphore.trySetPermits(rule.getPermits());
        return semaphore;
    }
}
