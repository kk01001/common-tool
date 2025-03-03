package io.github.kk01001.design.strategy;

/**
 * 通用策略接口
 * @param <T> 入参类型
 * @param <R> 返回值类型
 */
public interface IStrategy<T, R> {
    /**
     * 策略执行方法
     */
    default R execute(T param) {
        return null;
    }

    default void executeVoid(T param) {

    }
}