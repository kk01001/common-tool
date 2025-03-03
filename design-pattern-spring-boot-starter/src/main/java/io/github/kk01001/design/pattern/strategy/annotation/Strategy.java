package io.github.kk01001.design.pattern.strategy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 用于标记策略实现类，指定策略类型和所属的策略枚举
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Strategy {
    /**
     * 策略类型枚举的Class
     * @return 策略枚举类
     */
    Class<? extends Enum<?>> strategyEnum();
    
    /**
     * 具体策略类型
     * @return 策略类型标识
     */
    String strategyType();
}