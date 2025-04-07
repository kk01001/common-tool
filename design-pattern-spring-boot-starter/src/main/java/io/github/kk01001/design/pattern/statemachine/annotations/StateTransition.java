package io.github.kk01001.design.pattern.statemachine.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态转换注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StateTransition {

    /**
     * 源状态
     */
    String source();

    /**
     * 目标状态
     */
    String target();

    /**
     * 触发事件
     */
    String event();

} 