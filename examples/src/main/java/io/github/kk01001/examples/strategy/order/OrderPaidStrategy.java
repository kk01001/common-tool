package io.github.kk01001.examples.strategy.order;

import io.github.kk01001.design.pattern.strategy.annotation.Strategy;
import org.springframework.stereotype.Component;

@Component
@Strategy(strategyEnum = OrderStatusEnum.class, strategyType = "PAID")
public class OrderPaidStrategy implements IStrategy<OrderParam, OrderResult> {
    @Override
    public OrderResult execute(OrderParam param) {
        // 模拟订单支付完成处理逻辑
        return OrderResult.builder()
                .orderId(param.getOrderId())
                .oldStatus("CREATED")
                .newStatus("PAID")
                .success(true)
                .message("订单支付完成")
                .build();
    }
}