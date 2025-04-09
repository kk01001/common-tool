package io.github.kk01001.examples.design.statemachine;

import io.github.kk01001.design.pattern.statemachine.annotations.StateMachineDefinition;
import io.github.kk01001.design.pattern.statemachine.annotations.StateTransition;
import io.github.kk01001.design.pattern.statemachine.annotations.TransitionGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 订单状态处理服务
 */
@StateMachineDefinition(name = "orderStateMachine", initialState = "CREATED", stateClass = OrderState.class)
@Service
public class StateMachineOrderStateService {

    private static final Logger logger = LoggerFactory.getLogger(StateMachineOrderStateService.class);

    @TransitionGuard(spEL = {
            "#context?.amount > 10",
            "#context.amount > 101"
    })
    @StateTransition(source = "CREATED", target = "PAID", event = "PAY")
    public OrderState handlePayment(OrderState from, OrderEvent event, OrderContext context) {
        logger.info("订单[{}]支付完成", context.getOrderId());
        return OrderState.PAID;
    }

    @StateTransition(source = "PAID", target = "SHIPPED", event = "SHIP")
    public OrderState handleShipping(OrderState from, OrderEvent event, OrderContext context) {
        logger.info("订单[{}]已发货", context.getOrderId());
        return OrderState.SHIPPED;
    }

    @StateTransition(source = "SHIPPED", target = "FINISHED", event = "CONFIRM")
    public OrderState handleConfirmation(OrderState from, OrderEvent event, OrderContext context) {
        logger.info("订单[{}]已确认收货", context.getOrderId());
        return OrderState.FINISHED;
    }

    @TransitionGuard(OrderAmountTransitionGuard.class)
    @StateTransition(source = "CREATED", target = "CANCELLED", event = "CANCEL")
    public OrderState handleCancelFromCreated(OrderState from, OrderEvent event, OrderContext context) {
        logger.info("未支付订单[{}]已取消", context.getOrderId());
        return OrderState.CANCELLED;
    }

    @StateTransition(source = "PAID", target = "CANCELLED", event = "CANCEL")
    public OrderState handleCancelFromPaid(OrderState from, OrderEvent event, OrderContext context) {
        logger.info("已支付订单[{}]已取消，需要退款", context.getOrderId());
        return OrderState.CANCELLED;
    }
} 