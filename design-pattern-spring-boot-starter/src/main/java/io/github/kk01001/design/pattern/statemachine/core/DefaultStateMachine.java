package io.github.kk01001.design.pattern.statemachine.core;

import io.github.kk01001.design.pattern.statemachine.guard.StateTransitionGuardException;
import io.github.kk01001.design.pattern.statemachine.persister.StatePersister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 默认状态机实现
 */
@Slf4j
public class DefaultStateMachine<S, E, C> implements StateMachine<S, E, C> {

    private final StatePersister<S, C> statePersister;
    private final List<StateTransitionHandler<S, E, C>> handlers;
    private final S initialState;
    private final String machineName;
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

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
        Assert.hasText(machineId, "MachineId must not be empty");
        Assert.notNull(context, "Context must not be null");

        log.info("[{}][{}] 状态机启动", machineName, machineId);

        S currentState = getCurrentState(machineId, context);
        if (currentState == null) {
            statePersister.write(machineName, machineId, context, initialState);
            log.info("[{}][{}] 初始化状态: {}", machineName, machineId, initialState);
        }
    }

    @Override
    public void stop(String machineId, C context) {
        Assert.hasText(machineId, "MachineId must not be empty");
        Assert.notNull(context, "Context must not be null");

        log.info("[{}][{}] 状态机停止", machineName, machineId);
        statePersister.remove(machineName, machineId, context);
    }

    /**
     * 添加状态转换处理器
     */
    @Override
    public void addTransitionHandler(StateTransitionHandler<S, E, C> handler) {
        Assert.notNull(handler, "Handler must not be null");
        handlers.add(handler);
        log.info("[{}] 添加状态转换处理器: {} -> {} [{}]",
                machineName, handler.getSourceState(), handler.getTargetState(), handler.getEvent());
    }

    @Override
    public S sendEvent(String machineId, E event, C context) {
        Assert.hasText(machineId, "MachineId must not be empty");
        Assert.notNull(event, "Event must not be null");
        Assert.notNull(context, "Context must not be null");

        // 获取锁对象
        String lockKey = machineName + ":" + machineId;
        ReentrantLock lock = locks.computeIfAbsent(lockKey, k -> new ReentrantLock());

        // 同步锁，确保线程安全
        lock.lock();
        try {
            // 获取当前状态
            S currentState = getCurrentState(machineId, context);
            if (currentState == null) {
                currentState = initialState;
                statePersister.write(machineName, machineId, context, currentState);
            }

            log.info("[{}][{}] 接收事件: {}, 当前状态: {}", machineName, machineId, event, currentState);

            // 查找匹配的处理器
            final S state = currentState;
            Optional<StateTransitionHandler<S, E, C>> handlerOpt = handlers.stream()
                    .filter(h -> h.matches(state, event))
                    .findFirst();

            if (handlerOpt.isPresent()) {
                StateTransitionHandler<S, E, C> handler = handlerOpt.get();

                // 检查守卫条件
                if (!handler.evaluateGuards(state, event, context)) {
                    String reason = handler.getGuardRejectionReason(state, event, context);
                    log.warn("[{}][{}] 状态转换被守卫拒绝: {} -> {} [{}], 原因: {}",
                            machineName, machineId, state, handler.getTargetState(), event, reason);

                    throw new StateTransitionGuardException(
                            reason, machineId, machineName, state, event);
                }

                try {
                    // 执行状态转换
                    S newState = handler.handleTransition(state, event, context);
                    // 持久化新状态
                    statePersister.write(machineName, machineId, context, newState);

                    log.info("[{}][{}] 状态转换成功: {} -> {} [{}]",
                            machineName, machineId, state, newState, event);

                    return newState;
                } catch (Exception e) {
                    log.error("[{}][{}] 状态转换异常: {} -> {} [{}]",
                            machineName, machineId, state, handler.getTargetState(), event, e);
                    throw e;
                }
            }
            log.warn("[{}][{}] 未找到匹配的状态转换处理器: {} [{}]",
                    machineName, machineId, state, event);

            return currentState;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public S getCurrentState(String machineId, C context) {
        Assert.hasText(machineId, "MachineId must not be empty");
        Assert.notNull(context, "Context must not be null");
        return statePersister.read(machineName, machineId, context);
    }

    @Override
    public String getMachineName() {
        return machineName;
    }
} 