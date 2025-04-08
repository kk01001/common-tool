package io.github.kk01001.design.pattern.statemachine.core;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态机接口
 */
public interface StateMachine<S, E, C> {

    /**
     * 启动状态机
     *
     * @param machineId 状态机唯一标识
     * @param context   上下文
     */
    void start(String machineId, C context);

    /**
     * 停止状态机
     *
     * @param machineId 状态机唯一标识
     * @param context   上下文
     */
    void stop(String machineId, C context);

    /**
     * 添加状态转换处理器
     *
     * @param handler 状态转换处理器
     */
    void addTransitionHandler(StateTransitionHandler<S, E, C> handler);

    /**
     * 发送事件
     *
     * @param machineId 状态机唯一标识
     * @param event     事件
     * @param context   上下文
     * @return 最终状态
     */
    S sendEvent(String machineId, E event, C context);

    /**
     * 获取当前状态
     *
     * @param machineId 状态机唯一标识
     * @param context   上下文
     * @return 当前状态
     */
    S getCurrentState(String machineId, C context);

    /**
     * 获取状态机名称
     *
     * @return 状态机名称
     */
    String getMachineName();
}