package io.github.kk01001.design.pattern.statemachine.core;

import io.github.kk01001.design.pattern.statemachine.guard.GuardConditionFactory;
import io.github.kk01001.design.pattern.statemachine.guard.StateTransitionGuard;
import org.springframework.context.expression.BeanFactoryResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态转换处理器
 */
public interface StateTransitionHandler<S, E, C> {

    /**
     * 处理状态转换
     *
     * @param from 源状态
     * @param event 事件
     * @param context 上下文
     * @return 目标状态
     */
    S handleTransition(S from, E event, C context);

    /**
     * 获取源状态
     *
     * @return 源状态
     */
    S getSourceState();

    /**
     * 获取目标状态
     *
     * @return 目标状态
     */
    S getTargetState();

    /**
     * 获取事件
     *
     * @return 事件
     */
    E getEvent();

    /**
     * 获取守卫条件列表
     *
     * @return 守卫条件列表
     */
    default List<StateTransitionGuard<S, E, C>> getGuards() {
        return new ArrayList<>();
    }

    /**
     * 添加守卫条件
     *
     * @param guard 守卫条件
     */
    default void addGuard(StateTransitionGuard<S, E, C> guard) {
        getGuards().add(guard);
    }

    /**
     * 评估所有守卫条件
     *
     * @param from    源状态
     * @param event   事件
     * @param context 上下文
     * @return 是否满足所有守卫条件
     */
    default boolean evaluateGuards(S from, E event, C context) {
        if (getGuards().isEmpty()) {
            return true;
        }

        for (StateTransitionGuard<S, E, C> guard : getGuards()) {
            if (!guard.evaluate(from, event, context)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取守卫拒绝的原因
     *
     * @param from    源状态
     * @param event   事件
     * @param context 上下文
     * @return 拒绝原因，如果满足条件则返回null
     */
    default String getGuardRejectionReason(S from, E event, C context) {
        if (getGuards().isEmpty()) {
            return null;
        }

        for (StateTransitionGuard<S, E, C> guard : getGuards()) {
            if (!guard.evaluate(from, event, context)) {
                return guard.getRejectionReason();
            }
        }

        return null;
    }

    /**
     * 匹配事件和状态
     *
     * @param state 当前状态
     * @param event 事件
     * @return 是否匹配
     */
    default boolean matches(S state, E event) {
        return getSourceState().equals(state) && getEvent().equals(event);
    }

    /**
     * 添加SpEL表达式守卫
     *
     * @param expression SpEL表达式
     * @return 当前处理器
     */
    default StateTransitionHandler<S, E, C> withSpElGuard(String expression) {
        this.addGuard(GuardConditionFactory.createSpElGuard(expression));
        return this;
    }

    /**
     * 添加SpEL表达式守卫
     *
     * @param expression  SpEL表达式
     * @param beanFactory BeanFactoryResolver
     * @return 当前处理器
     */
    default StateTransitionHandler<S, E, C> withSpElGuard(String expression, BeanFactoryResolver beanFactory) {
        this.addGuard(GuardConditionFactory.createSpElGuard(expression, beanFactory));
        return this;
    }
} 