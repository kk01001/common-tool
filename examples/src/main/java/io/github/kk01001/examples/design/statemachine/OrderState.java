package io.github.kk01001.examples.design.statemachine;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 订单状态枚举
 */
public enum OrderState {
    /**
     * 已创建
     */
    CREATED,

    /**
     * 已支付
     */
    PAID,

    /**
     * 已发货
     */
    SHIPPED,

    /**
     * 已完成
     */
    FINISHED,

    /**
     * 已取消
     */
    CANCELLED;
}