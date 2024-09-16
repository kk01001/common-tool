package io.github.kk01001.lock.core.impl;

import io.github.kk01001.lock.core.LockStrategy;
import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.model.Rule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * @date 2024-09-16 12:20:00
 * @description redisson 锁
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedissonLockStrategyImpl implements LockStrategy {

    private final RedissonClient redissonClient;

    @Override
    public LockType getType() {
        return LockType.REDISSON_SEMAPHORE;
    }

    @SneakyThrows
    @Override
    public void lock(Rule rule) {
        RLock lock = getLock(rule);
        lock.lock();
    }

    @SneakyThrows
    @Override
    public boolean tryLock(Rule rule) {
        RLock lock = getLock(rule);
        return lock.tryLock(rule.getTimeout(), rule.getTimeUnit());
    }

    @Override
    public void unlock(Rule rule) {
        RLock lock = getLock(rule);
        lock.unlock();
    }

    private RLock getLock(Rule rule) {
        String key = rule.getKey();
        return redissonClient.getLock(key);
    }
}
