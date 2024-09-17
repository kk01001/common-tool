package io.github.kk01001.lock.core;

import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.exception.LockException;
import io.github.kk01001.lock.model.LockRule;
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

    public void lock(LockRule lockRule) {
        if (Objects.isNull(lockRule)) {
            return;
        }
        if (disable(lockRule)) {
            return;
        }
        LockType lockType = lockRule.getLockType();
        Optional<LockStrategy> lockStrategy = Optional.ofNullable(STRATEGY_MAP.get(lockType));
        if (lockStrategy.isPresent()) {
            lockStrategy.get().lock(lockRule);
            return;
        }
        throw new LockException("未找到对应的锁策略类型: " + lockType);
    }

    public boolean tryLock(LockRule lockRule) {
        if (Objects.isNull(lockRule)) {
            return true;
        }
        if (disable(lockRule)) {
            return true;
        }
        LockType lockType = lockRule.getLockType();
        Optional<LockStrategy> lockStrategy = Optional.ofNullable(STRATEGY_MAP.get(lockType));
        if (lockStrategy.isPresent()) {
            return lockStrategy.get().tryLock(lockRule);
        }
        throw new LockException("未找到对应的锁策略类型: " + lockType);
    }

    public void unlock(LockRule lockRule) {
        if (Objects.isNull(lockRule)) {
            return;
        }
        if (disable(lockRule)) {
            return;
        }
        LockType lockType = lockRule.getLockType();
        Optional<LockStrategy> lockStrategy = Optional.ofNullable(STRATEGY_MAP.get(lockType));
        if (lockStrategy.isPresent()) {
            lockStrategy.get().unlock(lockRule);
            return;
        }
        throw new LockException("未找到对应的锁策略类型: " + lockType);
    }

    private boolean disable(LockRule lockRule) {
        return !Boolean.TRUE.equals(lockRule.getEnable());
    }
}
