package io.github.kk01001.design.pattern.statemachine.core;

import io.github.kk01001.design.pattern.statemachine.persister.StatePersister;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态机构建器
 */
public class StateMachineBuilder<S, E, C> {

    private StatePersister<S, C> persister;
    private List<StateTransitionHandler<S, E, C>> handlers = new ArrayList<>();
    private S initialState;
    private String machineName;

    /**
     * 设置状态持久化器
     *
     * @param persister 状态持久化器
     * @return 当前构建器
     */
    public StateMachineBuilder<S, E, C> setPersister(StatePersister<S, C> persister) {
        this.persister = persister;
        return this;
    }

    /**
     * 添加状态转换处理器
     *
     * @param handler 状态转换处理器
     * @return 当前构建器
     */
    public StateMachineBuilder<S, E, C> addHandler(StateTransitionHandler<S, E, C> handler) {
        this.handlers.add(handler);
        return this;
    }

    /**
     * 设置处理器列表
     *
     * @param handlers 处理器列表
     * @return 当前构建器
     */
    public StateMachineBuilder<S, E, C> setHandlers(List<StateTransitionHandler<S, E, C>> handlers) {
        this.handlers = new ArrayList<>(handlers);
        return this;
    }

    /**
     * 设置初始状态
     *
     * @param initialState 初始状态
     * @return 当前构建器
     */
    public StateMachineBuilder<S, E, C> setInitialState(S initialState) {
        this.initialState = initialState;
        return this;
    }

    /**
     * 设置状态机名称
     *
     * @param machineName 状态机名称
     * @return 当前构建器
     */
    public StateMachineBuilder<S, E, C> setMachineName(String machineName) {
        this.machineName = machineName;
        return this;
    }

    /**
     * 构建状态机
     *
     * @return 状态机实例
     */
    public StateMachine<S, E, C> build() {
        Assert.notNull(persister, "Persister must not be null");
        Assert.notNull(initialState, "InitialState must not be null");
        Assert.hasText(machineName, "MachineName must not be empty");

        return new DefaultStateMachine<>(persister, handlers, initialState, machineName);
    }
} 