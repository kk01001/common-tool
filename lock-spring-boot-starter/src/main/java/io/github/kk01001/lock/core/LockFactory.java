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

    }

    public boolean tryLock(Rule rule) {
        if (Objects.isNull(rule)) {
            return true;
        }
        if (!Boolean.TRUE.equals(rule.getEnable())) {
            return true;
        }
        LockType lockType = rule.getLockType();
        return Optional.ofNullable(STRATEGY_MAP.get(lockType))
                .map(strategy -> strategy.tryLock(rule))
                .orElseThrow(() -> new LockException("未找到对应的限流策略类型: " + lockType));
    }

    public void unlock(Rule rule) {

    }
}
