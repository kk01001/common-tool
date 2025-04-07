package io.github.kk01001.examples.design.statemachine;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 订单上下文
 */
public class OrderContext {
    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单金额
     */
    private Double amount;

    public OrderContext(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return orderId;
    }
} 