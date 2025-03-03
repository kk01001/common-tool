package io.github.kk01001.examples.strategy.pay;

import io.github.kk01001.design.pattern.strategy.IStrategy;
import io.github.kk01001.design.pattern.strategy.annotation.Strategy;
import org.springframework.stereotype.Component;

@Component
@Strategy(strategyEnum = PayTypeEnum.class, strategyType = "ALIPAY")
public class AlipayStrategy implements IStrategy<PayParam, PayResult> {
    @Override
    public PayResult execute(PayParam param) {
        // 模拟支付宝支付
        return PayResult.builder()
                .orderId(param.getOrderId())
                .success(true)
                .message("支付宝支付成功")
                .tradeNo("ALI" + System.currentTimeMillis())
                .build();
    }
}