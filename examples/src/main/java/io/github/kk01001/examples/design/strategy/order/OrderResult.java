package io.github.kk01001.examples.design.strategy.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResult {
    private String orderId;
    private String oldStatus;
    private String newStatus;
    private boolean success;
    private String message;
}