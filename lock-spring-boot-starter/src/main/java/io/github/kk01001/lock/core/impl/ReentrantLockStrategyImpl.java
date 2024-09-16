package io.github.kk01001.lock.core.impl;

import io.github.kk01001.lock.core.LockStrategy;
import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.model.Rule;
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
        return LockType.SEMAPHORE;
    }

    @SneakyThrows
    @Override
    public void lock(Rule rule) {
        ReentrantLock lock = getReentrantLock(rule);
        lock.lock();
    }

    @SneakyThrows
    @Override
    public boolean tryLock(Rule rule) {
        Long timeout = rule.getTimeout();
        ReentrantLock lock = getReentrantLock(rule);
        return lock.tryLock(timeout, rule.getTimeUnit());
    }

    @Override
    public void unlock(Rule rule) {
        ReentrantLock lock = getReentrantLock(rule);
        lock.unlock();
    }

    private ReentrantLock getReentrantLock(Rule rule) {
        String key = rule.getKey();
        Boolean fair = rule.getFair();
        return MAP.computeIfAbsent(key, k -> new ReentrantLock(fair));
    }
}
