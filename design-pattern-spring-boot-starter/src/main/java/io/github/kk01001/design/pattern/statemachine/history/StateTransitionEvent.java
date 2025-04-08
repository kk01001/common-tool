package io.github.kk01001.design.pattern.statemachine.history;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态转换事件
 */
@Getter
public class StateTransitionEvent<S, E, C> extends ApplicationEvent {

    /**
     * 状态机名称
     */
    private final String machineName;

    /**
     * 状态机ID
     */
    private final String machineId;

    /**
     * 上下文
     */
    private final C context;

    /**
     * 源状态
     */
    private final S sourceState;

    /**
     * 目标状态
     */
    private final S targetState;

    /**
     * 触发事件
     */
    private final E event;

    /**
     * 转换是否成功
     */
    private final boolean success;

    /**
     * 如果失败，记录失败原因
     */
    private final String failureReason;

    /**
     * 转换发生时间
     */
    private final Long transitionTime;

    /**
     * 成功的状态转换事件构造函数
     *
     * @param source      事件源
     * @param machineName 状态机名称
     * @param machineId   状态机ID
     * @param context     上下文
     * @param sourceState 源状态
     * @param targetState 目标状态
     * @param event       触发事件
     */
    public StateTransitionEvent(Object source, String machineName, String machineId,
                                C context, S sourceState, S targetState, E event) {
        super(source);
        this.machineName = machineName;
        this.machineId = machineId;
        this.context = context;
        this.sourceState = sourceState;
        this.targetState = targetState;
        this.event = event;
        this.success = true;
        this.failureReason = null;
        this.transitionTime = System.currentTimeMillis();
    }

    /**
     * 失败的状态转换事件构造函数
     *
     * @param source        事件源
     * @param machineName   状态机名称
     * @param machineId     状态机ID
     * @param context       上下文
     * @param sourceState   源状态
     * @param event         触发事件
     * @param failureReason 失败原因
     */
    public StateTransitionEvent(Object source, String machineName, String machineId,
                                C context, S sourceState, E event, String failureReason) {
        super(source);
        this.machineName = machineName;
        this.machineId = machineId;
        this.context = context;
        this.sourceState = sourceState;
        this.targetState = null;
        this.event = event;
        this.success = false;
        this.failureReason = failureReason;
        this.transitionTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("StateTransitionEvent: %s[%s] %s -> %s [%s]",
                    machineName, machineId, sourceState, targetState, event);
        }
        return String.format("StateTransitionEvent: %s[%s] %s [%s] failed: %s",
                machineName, machineId, sourceState, event, failureReason);
    }
} 