package io.github.kk01001.lock.core.impl;

import io.github.kk01001.lock.core.LockStrategy;
import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.model.LockRule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author linshiqiang
 * @date 2024-09-16 12:20:00
 * @description 单机 可重入锁
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReentrantLockStrategyImpl implements LockStrategy {

    private static final Map<String, ReentrantLock> MAP = new ConcurrentHashMap<>();

    @Override
    public LockType getType() {
        return LockType.REENTRANT_LOCK;
    }

    @SneakyThrows
    @Override
    public void lock(LockRule lockRule) {
        ReentrantLock lock = getReentrantLock(lockRule);
        lock.lock();
    }

    @SneakyThrows
    @Override
    public boolean tryLock(LockRule lockRule) {
        Long timeout = lockRule.getTimeout();
        ReentrantLock lock = getReentrantLock(lockRule);
        return lock.tryLock(timeout, lockRule.getTimeUnit());
    }

    @Override
    public void unlock(LockRule lockRule) {
        ReentrantLock lock = getReentrantLock(lockRule);
        lock.unlock();
    }

    private ReentrantLock getReentrantLock(LockRule lockRule) {
        String key = lockRule.getKey();
        Boolean fair = lockRule.getFair();
        return MAP.computeIfAbsent(key, k -> new ReentrantLock(fair));
    }
}
