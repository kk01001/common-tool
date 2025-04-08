package io.github.kk01001.design.pattern.statemachine.event;

/**
 * @author kk01001
 * @date 2024-04-08 14:31:00
 * @description 状态转换事件类型
 */
public enum StateTransitionEventType {
    /**
     * 状态转换前
     */
    BEFORE_TRANSITION,

    /**
     * 状态转换成功
     */
    TRANSITION_COMPLETED,

    /**
     * 状态转换失败
     */
    TRANSITION_FAILED,

    /**
     * 状态初始化
     */
    STATE_INITIALIZED,

    /**
     * 状态销毁
     */
    STATE_DESTROYED
} 