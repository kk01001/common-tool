package io.github.kk01001.examples.design.statemachine;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 订单事件枚举
 */
public enum OrderEvent {
    /**
     * 支付事件
     */
    PAY,

    /**
     * 发货事件
     */
    SHIP,

    /**
     * 确认收货事件
     */
    CONFIRM,

    /**
     * 取消订单事件
     */
    CANCEL;

}