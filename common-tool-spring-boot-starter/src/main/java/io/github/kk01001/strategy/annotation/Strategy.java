package io.github.kk01001.strategy.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Strategy {
    /**
     * 策略类型枚举的Class
     */
    Class<? extends Enum<?>> strategyEnum();
    
    /**
     * 具体策略类型
     */
    String strategyType();
}