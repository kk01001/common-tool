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
import java.util.concurrent.Semaphore;

/**
 * @author linshiqiang
 * @date 2024-09-16 12:20:00
 * @description 单机信号量限并发
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemaphoreStrategyImpl implements LockStrategy {

    private static final Map<String, Semaphore> MAP = new ConcurrentHashMap<>();

    @Override
    public LockType getType() {
        return LockType.SEMAPHORE;
    }

    @SneakyThrows
    @Override
    public void lock(LockRule lockRule) {
        Semaphore semaphore = getSemaphore(lockRule);
        semaphore.acquire();
    }

    @SneakyThrows
    @Override
    public boolean tryLock(LockRule lockRule) {
        Long timeout = lockRule.getTimeout();
        Semaphore semaphore = getSemaphore(lockRule);
        return semaphore.tryAcquire(timeout, lockRule.getTimeUnit());
    }

    @Override
    public void unlock(LockRule lockRule) {
        Semaphore semaphore = getSemaphore(lockRule);
        semaphore.release();
    }

    private Semaphore getSemaphore(LockRule lockRule) {
        String key = lockRule.getKey();
        Integer permits = lockRule.getPermits();
        Boolean fair = lockRule.getFair();
        return MAP.computeIfAbsent(key, k -> new Semaphore(permits, fair));
    }
}
