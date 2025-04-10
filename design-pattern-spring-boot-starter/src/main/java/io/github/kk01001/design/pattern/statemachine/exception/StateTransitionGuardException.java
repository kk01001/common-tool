package io.github.kk01001.design.pattern.statemachine.exception;

import lombok.Getter;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态转换守卫条件不满足异常
 */
@Getter
public class StateTransitionGuardException extends RuntimeException {

    private final String machineId;
    private final String machineName;
    private final Object sourceState;
    private final Object event;

    public StateTransitionGuardException(String message, String machineId, String machineName,
                                         Object sourceState, Object event) {
        super(message);
        this.machineId = machineId;
        this.machineName = machineName;
        this.sourceState = sourceState;
        this.event = event;
    }

    public StateTransitionGuardException(String message, Object sourceState, Object event) {
        super(message);
        this.machineId = null;
        this.machineName = null;
        this.sourceState = sourceState;
        this.event = event;
    }

    @Override
    public String getMessage() {
        return String.format("[状态机: %s, ID: %s, 当前状态: %s, 事件: %s] error: %s ",
                machineName, machineId, sourceState, event, super.getMessage());
    }
} 