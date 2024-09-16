package io.github.kk01001.lock.core;

import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.exception.LockException;
import io.github.kk01001.lock.model.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:42:00
 * @description
 */
@Component
@RequiredArgsConstructor
public class LockFactory implements CommandLineRunner {

    private final List<LockStrategy> strategyList;

    private static final Map<LockType, LockStrategy> STRATEGY_MAP = new HashMap<>(8);

    @Override
    public void run(String... args) {
        for (LockStrategy strategy : strategyList) {
            STRATEGY_MAP.put(strategy.getType(), strategy);
        }
    }

    public void lock(Rule rule) {
        if (Objects.isNull(rule)) {
            return;
        }
        if (disable(rule)) {
            return;
        }
        LockType lockType = rule.getLockType();
        Optional<LockStrategy> lockStrategy = Optional.ofNullable(STRATEGY_MAP.get(lockType));
        if (lockStrategy.isPresent()) {
            lockStrategy.get().lock(rule);
            return;
        }
        throw new LockException("未找到对应的锁策略类型: " + lockType);
    }

    public boolean tryLock(Rule rule) {
        if (Objects.isNull(rule)) {
            return true;
        }
        if (disable(rule)) {
            return true;
        }
        LockType lockType = rule.getLockType();
        Optional<LockStrategy> lockStrategy = Optional.ofNullable(STRATEGY_MAP.get(lockType));
        if (lockStrategy.isPresent()) {
            return lockStrategy.get().tryLock(rule);
        }
        throw new LockException("未找到对应的锁策略类型: " + lockType);
    }

    public void unlock(Rule rule) {
        if (Objects.isNull(rule)) {
            return;
        }
        if (disable(rule)) {
            return;
        }
        LockType lockType = rule.getLockType();
        Optional<LockStrategy> lockStrategy = Optional.ofNullable(STRATEGY_MAP.get(lockType));
        if (lockStrategy.isPresent()) {
            lockStrategy.get().unlock(rule);
            return;
        }
        throw new LockException("未找到对应的锁策略类型: " + lockType);
    }

    private boolean disable(Rule rule) {
        return !Boolean.TRUE.equals(rule.getEnable());
    }
}
