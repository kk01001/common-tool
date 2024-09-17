package io.github.kk01001.lock.enums;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:38:00
 * @description
 */
public enum LockType {

    /**
     * 信号量
     */
    SEMAPHORE,

    /**
     * 可重入锁
     */
    REENTRANT_LOCK,

    /**
     * redisson 信号量
     */
    REDISSON_SEMAPHORE,

    /**
     * redisson 锁
     */
    REDISSON_LOCK,

    /**
     * redisson 公平锁
     */
    REDISSON_FAIR_LOCK,

    REDISSON_SPIN_LOCK,

    REDISSON_READ_WRITE_LOCK,

    /**
     * redis template 信号量
     */
    REDIS_TEMPLATE_SEMAPHORE,
}
