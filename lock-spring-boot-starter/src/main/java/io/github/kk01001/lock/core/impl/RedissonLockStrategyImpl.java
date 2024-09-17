package io.github.kk01001.lock.core.impl;

import io.github.kk01001.lock.core.LockStrategy;
import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.model.LockRule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * @date 2024-09-16 12:20:00
 * @description redisson 锁
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(RedissonClient.class)
public class RedissonLockStrategyImpl implements LockStrategy {

    private final RedissonClient redissonClient;

    @Override
    public LockType getType() {
        return LockType.REDISSON_LOCK;
    }

    @SneakyThrows
    @Override
    public void lock(LockRule lockRule) {
        RLock lock = getLock(lockRule);
        lock.lock();
    }

    @SneakyThrows
    @Override
    public boolean tryLock(LockRule lockRule) {
        RLock lock = getLock(lockRule);
        return lock.tryLock(lockRule.getTimeout(), lockRule.getTimeUnit());
    }

    @Override
    public void unlock(LockRule lockRule) {
        RLock lock = getLock(lockRule);
        lock.unlock();
    }

    private RLock getLock(LockRule lockRule) {
        String key = lockRule.getKey();
        return redissonClient.getLock(key);
    }
}
