package io.github.kk01001.examples.design.strategy.pay;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayParam {
    private String orderId;
    private BigDecimal amount;
    private String userId;
}