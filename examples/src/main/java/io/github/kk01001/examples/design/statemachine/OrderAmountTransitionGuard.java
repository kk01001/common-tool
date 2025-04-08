package io.github.kk01001.examples.design.statemachine;

import io.github.kk01001.design.pattern.statemachine.guard.StateTransitionGuard;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 订单金额守卫条件，只有订单金额大于等于100才允许支付
 */
public class OrderAmountTransitionGuard implements StateTransitionGuard<OrderState, OrderEvent, OrderContext> {

    private static final double MINIMUM_AMOUNT = 100.0;

    @Override
    public boolean evaluate(OrderState sourceState, OrderEvent event, OrderContext context) {
        // 只针对支付事件进行检查
        if (event == OrderEvent.PAY) {
            return context.getAmount() >= MINIMUM_AMOUNT;
        }
        // 其他事件不进行限制
        return true;
    }

    @Override
    public String getRejectionReason() {
        return "订单金额必须大于等于" + MINIMUM_AMOUNT + "才能支付";
    }
} 