package io.github.kk01001.examples.design.strategy.pay;

import io.github.kk01001.design.pattern.strategy.IStrategy;
import io.github.kk01001.design.pattern.strategy.annotation.Strategy;
import org.springframework.stereotype.Component;

@Component
@Strategy(strategyEnum = PayTypeEnum.class, strategyType = "WECHAT")
public class WechatStrategy implements IStrategy<PayParam, PayResult> {
    @Override
    public PayResult execute(PayParam param) {
        // 模拟微信支付
        return PayResult.builder()
                .orderId(param.getOrderId())
                .success(true)
                .message("微信支付成功")
                .tradeNo("WX" + System.currentTimeMillis())
                .build();
    }
}