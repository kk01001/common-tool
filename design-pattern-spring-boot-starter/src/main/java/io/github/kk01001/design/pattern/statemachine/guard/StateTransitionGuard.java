package io.github.kk01001.design.pattern.statemachine.guard;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态转换守卫接口，用于条件化状态转换判断
 */
public interface StateTransitionGuard<S, E, C> {

    /**
     * 判断是否允许状态转换
     *
     * @param sourceState 源状态
     * @param event       事件
     * @param context     上下文
     * @return 是否允许转换
     */
    boolean evaluate(S sourceState, E event, C context);
    
    /**
     * 获取拒绝转换时的原因描述
     *
     * @return 拒绝原因
     */
    default String getRejectionReason() {
        return "状态转换条件不满足";
    }

}