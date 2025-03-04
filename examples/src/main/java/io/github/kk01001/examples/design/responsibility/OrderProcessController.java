package io.github.kk01001.examples.design.responsibility;

import io.github.kk01001.design.pattern.responsibility.ResponsibilityChainContext;
import io.github.kk01001.design.pattern.responsibility.ResponsibilityChainFactory;
import io.github.kk01001.design.pattern.responsibility.ResponsibilityChainHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单处理责任链控制器
 * 演示如何通过接口动态注册和执行责任链处理器
 */
// @RestController
@RequestMapping("/api/order-process")
public class OrderProcessController {

    @Autowired
    private ResponsibilityChainFactory responsibilityChainFactory;

    /**
     * 注册处理器
     */
    @PostMapping("/handlers/{groupName}")
    public String registerHandler(@PathVariable String groupName, @RequestParam int order) {
        // 创建一个新的处理器实例
        ResponsibilityChainHandler<OrderProcessExample.Order, String> handler = 
            new ResponsibilityChainHandler<OrderProcessExample.Order, String>() {
                @Override
                public String handle(ResponsibilityChainContext<OrderProcessExample.Order, String> context) {
                    OrderProcessExample.Order order = context.getData();
                    // 模拟处理逻辑
                    System.out.println("Dynamic handler processing order: " + order.getOrderId());
                    context.recordHandler("DynamicHandler-" + order);
                    return null;
                }

                @Override
                public void handleVoid(ResponsibilityChainContext<OrderProcessExample.Order, String> context) {
                }

                @Override
                public int getOrder() {
                    return order;
                }
            };

        // 注册处理器
        responsibilityChainFactory.registerHandler(groupName, handler);
        return "Handler registered successfully";
    }

    /**
     * 执行责任链
     */
    @PostMapping("/execute/{groupName}")
    public String executeChain(@PathVariable String groupName, @RequestBody OrderProcessExample.Order order) throws Exception {
        // 创建责任链上下文
        ResponsibilityChainContext<OrderProcessExample.Order, String> context = 
            new ResponsibilityChainContext<OrderProcessExample.Order, String>()
                .setData(order);

        // 执行责任链
        String result = responsibilityChainFactory.execute(groupName, context);

        // 返回执行结果
        return String.format("Result: %s\nHandler History: %s", 
            result, 
            context.getHandlerHistory());
    }

    /**
     * 移除处理器
     */
    @DeleteMapping("/handlers/{groupName}/{handlerClass}")
    public String removeHandler(@PathVariable String groupName, @PathVariable String handlerClass) {
        try {
            // 获取处理器类
            @SuppressWarnings("unchecked")
            Class<? extends ResponsibilityChainHandler<?, ?>> clazz = 
                (Class<? extends ResponsibilityChainHandler<?, ?>>) Class.forName(handlerClass);
            
            // 移除处理器
            responsibilityChainFactory.removeHandler(groupName, clazz);
            return "Handler removed successfully";
        } catch (ClassNotFoundException e) {
            return "Handler class not found: " + e.getMessage();
        }
    }
}