package io.github.kk01001.design.pattern.statemachine.annotations;

import io.github.kk01001.design.pattern.statemachine.guard.StateTransitionGuard;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态转换守卫条件注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TransitionGuard {

    /**
     * 守卫条件类
     * 必须实现StateTransitionGuard接口
     */
    Class<? extends StateTransitionGuard<?, ?, ?>>[] value();

} 