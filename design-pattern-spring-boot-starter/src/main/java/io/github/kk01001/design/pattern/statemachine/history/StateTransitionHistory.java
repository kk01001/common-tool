package io.github.kk01001.design.pattern.statemachine.history;

import lombok.Getter;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态转换历史记录
 */
@Getter
public class StateTransitionHistory<S, E> {

    /**
     * 状态机名称
     */
    private final String machineName;

    /**
     * 状态机ID
     */
    private final String machineId;

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
     * 转换上下文
     */
    private Object context;

    /**
     * 转换成功标志
     */
    private final boolean success;

    /**
     * 如果转换失败，记录失败原因
     */
    private final String failureReason;

    /**
     * 状态转换时间
     */
    private final Long transitionTime;

    /**
     * 附加信息，可以存储额外的转换上下文
     */
    private final String additionalInfo;

    private StateTransitionHistory(Builder<S, E> builder) {
        this.machineName = builder.machineName;
        this.machineId = builder.machineId;
        this.sourceState = builder.sourceState;
        this.targetState = builder.targetState;
        this.event = builder.event;
        this.context = builder.context;
        this.success = builder.success;
        this.failureReason = builder.failureReason;
        this.transitionTime = builder.transitionTime;
        this.additionalInfo = builder.additionalInfo;
    }

    @Override
    public String toString() {
        return "StateTransitionHistory{" +
                "machineName='" + machineName + '\'' +
                ", machineId='" + machineId + '\'' +
                ", sourceState=" + sourceState +
                ", targetState=" + targetState +
                ", event=" + event +
                ", success=" + success +
                ", transitionTime=" + transitionTime +
                (failureReason != null ? ", failureReason='" + failureReason + '\'' : "") +
                (additionalInfo != null ? ", additionalInfo='" + additionalInfo + '\'' : "") +
                '}';
    }

    /**
     * 使用Builder模式构建历史记录
     */
    public static class Builder<S, E> {
        private String machineName;
        private String machineId;
        private S sourceState;
        private S targetState;
        private E event;
        private Object context;
        private boolean success = true;
        private String failureReason;
        private Long transitionTime;
        private String additionalInfo;

        public Builder<S, E> machineName(String machineName) {
            this.machineName = machineName;
            return this;
        }

        public Builder<S, E> machineId(String machineId) {
            this.machineId = machineId;
            return this;
        }

        public Builder<S, E> sourceState(S sourceState) {
            this.sourceState = sourceState;
            return this;
        }

        public Builder<S, E> targetState(S targetState) {
            this.targetState = targetState;
            return this;
        }

        public Builder<S, E> event(E event) {
            this.event = event;
            return this;
        }

        public Builder<S, E> context(Object context) {
            this.context = context;
            return this;
        }

        public Builder<S, E> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<S, E> failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public Builder<S, E> transitionTime(Long transitionTime) {
            this.transitionTime = transitionTime;
            return this;
        }

        public Builder<S, E> additionalInfo(String additionalInfo) {
            this.additionalInfo = additionalInfo;
            return this;
        }

        public StateTransitionHistory<S, E> build() {
            return new StateTransitionHistory<>(this);
        }
    }
} 