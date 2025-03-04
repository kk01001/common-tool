package io.github.kk01001.examples.design.responsibility;

import io.github.kk01001.design.pattern.responsibility.ResponsibilityChain;
import io.github.kk01001.design.pattern.responsibility.ResponsibilityChainContext;
import io.github.kk01001.design.pattern.responsibility.ResponsibilityChainFactory;
import io.github.kk01001.design.pattern.responsibility.ResponsibilityChainHandler;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 订单处理责任链示例
 */
public class OrderProcessExample {

    @Data
    public static class Order {
        private String orderId;
        private BigDecimal amount;
        private Integer quantity;
        private String userId;
        private boolean validated;
        private boolean stockChecked;
        private boolean paid;
    }

    /**
     * 订单验证处理器
     */
    @Component
    @ResponsibilityChain(value = "orderProcess", order = 1)
    public static class OrderValidationHandler implements ResponsibilityChainHandler<Order, String> {

        @Override
        public String handle(ResponsibilityChainContext<Order, String> context) {
            Order order = context.getData();
            // 模拟订单验证逻辑
            if (order.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                context.terminate();
                return "订单金额必须大于0";
            }
            order.setValidated(true);
            context.recordHandler("OrderValidationHandler");
            return null;
        }

        @Override
        public boolean shouldTerminated(ResponsibilityChainContext<Order, String> context) {
            return ResponsibilityChainHandler.super.shouldTerminated(context);
        }
    }

    /**
     * 库存检查处理器
     */
    @Component
    @ResponsibilityChain(value = "orderProcess", order = 2)
    public static class StockCheckHandler implements ResponsibilityChainHandler<Order, String> {

        @Override
        public String handle(ResponsibilityChainContext<Order, String> context) {
            Order order = context.getData();
            // 模拟库存检查逻辑
            if (order.getQuantity() > 100) {
                context.terminate();
                return "库存不足";
            }
            order.setStockChecked(true);
            context.recordHandler("StockCheckHandler");
            return null;
        }

        @Override
        public boolean shouldTerminated(ResponsibilityChainContext<Order, String> context) {
            return ResponsibilityChainHandler.super.shouldTerminated(context);
        }
    }

    /**
     * 支付处理器
     */
    @Component
    @ResponsibilityChain(value = "orderProcess", order = 3)
    public static class PaymentHandler implements ResponsibilityChainHandler<Order, String> {

        @Override
        public String handle(ResponsibilityChainContext<Order, String> context) {
            Order order = context.getData();
            // 模拟支付处理逻辑
            order.setPaid(true);
            context.recordHandler("PaymentHandler");
            return "订单处理成功";
        }

        @Override
        public boolean shouldTerminated(ResponsibilityChainContext<Order, String> context) {
            return ResponsibilityChainHandler.super.shouldTerminated(context);
        }
    }

    /**
     * 测试责任链执行
     */
    public static void main(String[] args) throws Exception {
        // 创建Spring容器并获取ResponsibilityChainFactory实例
        // 注意：实际项目中应通过Spring自动注入获取Factory实例
        ResponsibilityChainFactory factory = new ResponsibilityChainFactory();

        // 手动注册处理器
        factory.registerHandler("orderProcess", new OrderValidationHandler());
        factory.registerHandler("orderProcess", new StockCheckHandler());
        factory.registerHandler("orderProcess", new PaymentHandler());

        // 创建订单对象
        Order order = new Order();
        order.setOrderId("ORDER001");
        order.setAmount(new BigDecimal("100.00"));
        order.setQuantity(50);
        order.setUserId("USER001");

        // 创建责任链上下文
        ResponsibilityChainContext<Order, String> context = new ResponsibilityChainContext<Order, String>()
                .setData(order);

        // 执行责任链
        String result = factory.execute("orderProcess", context);

        // 输出处理结果
        System.out.println("处理结果: " + result);
        System.out.println("处理器执行历史: " + context.getHandlerHistory());
        System.out.println("订单状态: " +
                "validated=" + order.isValidated() + ", " +
                "stockChecked=" + order.isStockChecked() + ", " +
                "paid=" + order.isPaid());
    }
}