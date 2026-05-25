package io.github.archer099.examples.design.strategy.pay;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayResult {
    private String orderId;
    private boolean success;
    private String message;
    private String tradeNo;
}