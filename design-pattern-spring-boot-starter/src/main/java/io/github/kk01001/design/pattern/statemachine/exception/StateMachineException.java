package io.github.kk01001.design.pattern.statemachine.exception;

import lombok.Getter;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态转换守卫条件不满足异常
 */
@Getter
public class StateMachineException extends RuntimeException {

    private final String machineId;
    private final String machineName;
    private final Object state;
    private final Object event;

    public StateMachineException(String machineId, String machineName, String message) {
        super(message);
        this.machineId = machineId;
        this.machineName = machineName;
        this.state = null;
        this.event = null;
    }

    public StateMachineException(String machineId, String machineName, String message, Throwable cause) {
        super(message, cause);
        this.machineId = machineId;
        this.machineName = machineName;
        this.state = null;
        this.event = null;
    }

    public StateMachineException(String machineId, String machineName, Object state, Object event, String message) {
        super(message);
        this.machineId = machineId;
        this.machineName = machineName;
        this.state = state;
        this.event = event;
    }

    @Override
    public String getMessage() {
        return String.format("[状态机: %s, ID: %s] 异常信息: %s",
                machineName, machineId, super.getMessage());
    }
} 