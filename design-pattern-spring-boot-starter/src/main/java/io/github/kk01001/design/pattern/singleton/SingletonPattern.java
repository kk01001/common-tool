package io.github.kk01001.design.pattern.singleton;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 单例模式注解，标记需要使用单例模式的类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SingletonPattern {
    /**
     * 单例模式类型
     */
    Type type() default Type.LAZY;

    /**
     * 单例模式类型枚举
     */
    enum Type {
        /**
         * 懒加载
         */
        LAZY,
        /**
         * 饿汉式
         */
        EAGER,
        /**
         * 双重检查
         */
        DOUBLE_CHECK,
        /**
         * 枚举单例
         */
        ENUM
    }
}