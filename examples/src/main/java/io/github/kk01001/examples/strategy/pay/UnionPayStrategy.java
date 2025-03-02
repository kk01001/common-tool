package io.github.kk01001.examples.strategy.pay;

import io.github.kk01001.strategy.IStrategy;
import io.github.kk01001.strategy.annotation.Strategy;
import org.springframework.stereotype.Component;

@Component
@Strategy(strategyEnum = PayTypeEnum.class, strategyType = "UNION_PAY")
public class UnionPayStrategy implements IStrategy<PayParam, PayResult> {
    @Override
    public PayResult execute(PayParam param) {
        // 模拟银联支付
        return PayResult.builder()
                .orderId(param.getOrderId())
                .success(true)
                .message("银联支付成功")
                .tradeNo("UP" + System.currentTimeMillis())
                .build();
    }
}