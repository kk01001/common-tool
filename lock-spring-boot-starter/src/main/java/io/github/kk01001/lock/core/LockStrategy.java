package io.github.kk01001.lock.core;

import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.model.Rule;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:39:00
 * @description
 */
public interface LockStrategy {

    LockType getType();

    void lock(Rule rule);

    boolean tryLock(Rule rule);

    void unlock(Rule rule);

}
