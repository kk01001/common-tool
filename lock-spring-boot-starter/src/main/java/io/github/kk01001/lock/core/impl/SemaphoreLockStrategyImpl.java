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
import java.util.concurrent.Semaphore;

/**
 * @author linshiqiang
 * @date 2024-09-16 12:20:00
 * @description 单机信号量限并发
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemaphoreLockStrategyImpl implements LockStrategy {

    private static final Map<String, Semaphore> MAP = new ConcurrentHashMap<>();

    @Override
    public LockType getType() {
        return LockType.SEMAPHORE;
    }

    @SneakyThrows
    @Override
    public void lock(Rule rule) {
        Semaphore semaphore = getSemaphore(rule);
        semaphore.acquire();
    }

    @SneakyThrows
    @Override
    public boolean tryLock(Rule rule) {
        Long timeout = rule.getTimeout();
        Semaphore semaphore = getSemaphore(rule);
        return semaphore.tryAcquire(timeout, rule.getTimeUnit());
    }

    @Override
    public void unlock(Rule rule) {
        Semaphore semaphore = getSemaphore(rule);
        semaphore.release();
    }

    private Semaphore getSemaphore(Rule rule) {
        String key = rule.getKey();
        Integer permits = rule.getPermits();
        Boolean fair = rule.getFair();
        return MAP.computeIfAbsent(key, k -> new Semaphore(permits, fair));
    }
}
