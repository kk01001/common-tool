package io.github.kk01001.design.pattern.statemachine.core;

import io.github.kk01001.design.pattern.statemachine.persister.StatePersister;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态机构建器
 */
public class StateMachineBuilder<S, E, C> {

    private final List<StateTransitionHandler<S, E, C>> handlers = new ArrayList<>();
    private StatePersister<S, C> statePersister;
    private S initialState;
    private String machineName;

    /**
     * 添加状态转换处理器
     */
    public StateMachineBuilder<S, E, C> addTransition(StateTransitionHandler<S, E, C> handler) {
        handlers.add(handler);
        return this;
    }

    /**
     * 设置状态持久化器
     */
    public StateMachineBuilder<S, E, C> setPersister(StatePersister<S, C> persister) {
        this.statePersister = persister;
        return this;
    }

    /**
     * 设置初始状态
     */
    public StateMachineBuilder<S, E, C> setInitialState(S state) {
        this.initialState = state;
        return this;
    }

    /**
     * 设置状态机名称
     */
    public StateMachineBuilder<S, E, C> setMachineName(String name) {
        this.machineName = name;
        return this;
    }

    /**
     * 构建状态机
     */
    public StateMachine<S, E, C> build() {
        return new DefaultStateMachine<>(statePersister, handlers, initialState, machineName);
    }
} 