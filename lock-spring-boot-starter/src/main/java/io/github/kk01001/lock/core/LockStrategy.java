package io.github.archer099.lock.core;

import io.github.archer099.lock.enums.LockType;
import io.github.archer099.lock.model.LockRule;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:39:00
 * @description
 */
public interface LockStrategy {

    LockType getType();

    void lock(LockRule lockRule);

    boolean tryLock(LockRule lockRule);

    void unlock(LockRule lockRule);

}
