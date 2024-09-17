package io.github.kk01001.lock.core;

import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.model.LockRule;

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
