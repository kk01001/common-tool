package io.github.kk01001.design.pattern.statemachine.core;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态转换处理器接口
 */
public interface StateTransitionHandler<S, E, C> {
    /**
     * 处理状态转换
     *
     * @param from    源状态
     * @param event   事件
     * @param context 上下文
     * @return 目标状态
     */
    S handleTransition(S from, E event, C context);

    /**
     * 获取源状态
     */
    S getSourceState();

    /**
     * 获取目标状态
     */
    S getTargetState();

    /**
     * 获取事件
     */
    E getEvent();
} 