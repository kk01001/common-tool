package io.github.kk01001.design.pattern.statemachine.core;

import io.github.kk01001.design.pattern.statemachine.persister.StatePersister;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 默认状态机实现
 */
public class DefaultStateMachine<S, E, C> implements StateMachine<S, E, C> {

    private final StatePersister<S, C> statePersister;
    private final List<StateTransitionHandler<S, E, C>> handlers;
    private final S initialState;
    private final String machineName;

    public DefaultStateMachine(StatePersister<S, C> statePersister,
                               List<StateTransitionHandler<S, E, C>> handlers,
                               S initialState,
                               String machineName) {
        Assert.notNull(statePersister, "StatePersister must not be null");
        Assert.notNull(handlers, "Handlers must not be null");
        Assert.notNull(initialState, "InitialState must not be null");
        Assert.hasText(machineName, "MachineName must not be empty");
        this.statePersister = statePersister;
        this.handlers = new ArrayList<>(handlers);
        this.initialState = initialState;
        this.machineName = machineName;
    }

    @Override
    public void start(String machineId, C context) {
        statePersister.write(this.machineName, machineId, context, initialState);
    }

    @Override
    public void stop(String machineId, C context) {
        statePersister.remove(this.machineName, machineId, context);
    }

    /**
     * 添加状态转换处理器
     */
    @Override
    public void addTransitionHandler(StateTransitionHandler<S, E, C> handler) {
        Assert.notNull(handler, "Handler must not be null");
        handlers.add(handler);
    }

    @Override
    public S sendEvent(String machineId, E event, C context) {
        Assert.notNull(event, "Event must not be null");
        Assert.notNull(context, "Context must not be null");

        // 获取当前状态
        S currentState = getCurrentState(this.machineName, context);
        if (currentState == null) {
            currentState = initialState;
            statePersister.write(this.machineName, machineId, context, currentState);
        }

        // 查找匹配的处理器
        final S state = currentState;
        Optional<StateTransitionHandler<S, E, C>> handler = handlers.stream()
                .filter(h -> h.getSourceState().equals(state) && h.getEvent().equals(event))
                .findFirst();

        if (handler.isPresent()) {
            // 执行状态转换
            S newState = handler.get().handleTransition(state, event, context);
            // 持久化新状态
            statePersister.write(this.machineName, machineId, context, newState);
            return newState;
        }

        return currentState;
    }

    @Override
    public S getCurrentState(String machineId, C context) {
        Assert.notNull(context, "Context must not be null");
        return statePersister.read(this.machineName, machineId, context);
    }

    @Override
    public String getMachineName() {
        return machineName;
    }
} 