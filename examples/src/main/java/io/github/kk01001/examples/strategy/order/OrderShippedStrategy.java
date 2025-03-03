package io.github.kk01001.examples.strategy.order;

import io.github.kk01001.design.pattern.strategy.IStrategy;
import io.github.kk01001.design.pattern.strategy.annotation.Strategy;
import org.springframework.stereotype.Component;

@Component
@Strategy(strategyEnum = OrderStatusEnum.class, strategyType = "SHIPPED")
public class OrderShippedStrategy implements IStrategy<OrderParam, OrderResult> {
    @Override
    public OrderResult execute(OrderParam param) {
        // 模拟订单发货处理逻辑
        return OrderResult.builder()
                .orderId(param.getOrderId())
                .oldStatus("PAID")
                .newStatus("SHIPPED")
                .success(true)
                .message("订单已发货")
                .build();
    }
}