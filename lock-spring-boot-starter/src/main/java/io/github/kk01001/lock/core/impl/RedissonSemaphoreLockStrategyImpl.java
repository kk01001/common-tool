package io.github.kk01001.lock.core.impl;

import io.github.kk01001.lock.core.LockStrategy;
import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.model.LockRule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnClass(RedissonClient.class)
public class RedissonSemaphoreLockStrategyImpl implements LockStrategy {

    private final RedissonClient redissonClient;

    @Override
    public LockType getType() {
        return LockType.REDISSON_SEMAPHORE;
    }

    @SneakyThrows
    @Override
    public void lock(LockRule lockRule) {
        RSemaphore semaphore = getSemaphore(lockRule);
        semaphore.acquire();
    }

    @SneakyThrows
    @Override
    public boolean tryLock(LockRule lockRule) {
        RSemaphore semaphore = getSemaphore(lockRule);
        return semaphore.tryAcquire(Duration.ofMillis(lockRule.getTimeout()));
    }

    @Override
    public void unlock(LockRule lockRule) {
        RSemaphore semaphore = getSemaphore(lockRule);
        semaphore.release();
    }

    private RSemaphore getSemaphore(LockRule lockRule) {
        String key = lockRule.getKey();
        RSemaphore semaphore = redissonClient.getSemaphore(key);
        semaphore.trySetPermits(lockRule.getPermits());
        return semaphore;
    }
}
