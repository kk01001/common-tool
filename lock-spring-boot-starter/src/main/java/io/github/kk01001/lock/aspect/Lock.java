package io.github.kk01001.lock.aspect;

import io.github.kk01001.lock.enums.LockType;
import io.github.kk01001.lock.enums.RedisClientType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2024-09-06 21:40:00
 * @description
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lock {

    /**
     * 锁类型
     */
    LockType type() default LockType.REENTRANT_LOCK;

    /**
     * redis客户端类型
     */
    RedisClientType redisClientType() default RedisClientType.REDISSON;

    /**
     * 是否开启锁
     */
    boolean enable() default true;

    /**
     * 是否为阻塞锁
     */
    boolean block() default true;

    /**
     * lock key el
     * ex: key = "'lock:'+ #DataModel.code",
     */
    String key() default "defaultLockKey";

    /**
     * 信号量:
     * 总的凭证数
     */
    int permits() default 10;

    /**
     * 信号量:
     * true公平 false非公平
     */
    boolean fair() default true;

    /**
     * 锁超时时间
     */
    long timeout() default 1000L;

    /**
     * 单位
     */
    TimeUnit timeunit() default TimeUnit.MILLISECONDS;

    /**
     * 凭证el表达式方法
     */
    String permitsFunction() default "";

    /**
     * 自定义规则查询方法
     * 优先级最高
     */
    String ruleFunction() default "";
}
