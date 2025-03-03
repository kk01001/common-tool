package io.github.kk01001.design.pattern.strategy;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 策略模式的核心接口，定义了策略的执行方法
 * @param <T> 入参类型
 * @param <R> 返回值类型
 */
public interface IStrategy<T, R> {
    /**
     * 策略执行方法
     * @param param 策略参数
     * @return 策略执行结果
     */
    default R execute(T param) {
        return null;
    }

    /**
     * 无返回值的策略执行方法
     *
     * @param param 策略参数
     */
    default void executeVoid(T param) {

    }
}