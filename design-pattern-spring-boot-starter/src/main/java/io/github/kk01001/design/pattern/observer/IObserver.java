package io.github.kk01001.design.pattern.observer;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 观察者模式的核心接口，定义了观察者的基本行为
 * @param <T> 观察的数据类型
 */
public interface IObserver<T> {
    /**
     * 当被观察对象发生变化时的回调方法
     *
     * @param data 变化的数据
     */
    void onUpdate(T data);
    
    /**
     * 获取观察者优先级
     * 数值越小优先级越高
     *
     * @return 优先级值
     */
    default int getOrder() {
        return 0;
    }
    
    /**
     * 获取观察者名称
     *
     * @return 观察者名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}