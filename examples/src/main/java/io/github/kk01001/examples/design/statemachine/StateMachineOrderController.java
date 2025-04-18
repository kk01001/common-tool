package io.github.kk01001.examples.design.statemachine;

import io.github.kk01001.design.pattern.statemachine.StateMachineFactory;
import io.github.kk01001.design.pattern.statemachine.core.StateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 订单控制器
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class StateMachineOrderController {

    private final StateMachineFactory stateMachineFactory;

    private final Map<String, OrderContext> map = new HashMap<>();

    /**
     * 创建订单
     */
    @PostMapping
    public OrderContext createOrder(@RequestParam(required = false) Double amount) {
        OrderContext context = new OrderContext(UUID.randomUUID().toString());
        context.setAmount(amount != null ? amount : 100.0);
        // 初始状态将自动设置为 CREATED
        StateMachine<OrderState, OrderEvent, OrderContext> stateMachine = stateMachineFactory.getStateMachine(StateMachineOrderStateService.class);
        stateMachine.start(context.getOrderId(), context);
        map.put(context.getOrderId(), context);
        return context;
    }

    /**
     * 支付订单
     */
    @PostMapping("/{orderId}/pay")
    public OrderState payOrder(@PathVariable String orderId) {
        OrderContext context = map.get(orderId);
        StateMachine<OrderState, OrderEvent, OrderContext> stateMachine = stateMachineFactory.getStateMachine(StateMachineOrderStateService.class);
        return stateMachine.sendEvent(orderId, OrderEvent.PAY, context);
    }

    /**
     * 发货
     */
    @PostMapping("/{orderId}/ship")
    public OrderState shipOrder(@PathVariable String orderId) {
        OrderContext context = map.get(orderId);
        StateMachine<OrderState, OrderEvent, OrderContext> stateMachine = stateMachineFactory.getStateMachine(StateMachineOrderStateService.class);
        return stateMachine.sendEvent(orderId, OrderEvent.SHIP, context);
    }

    /**
     * 确认收货
     */
    @PostMapping("/{orderId}/confirm")
    public OrderState confirmOrder(@PathVariable String orderId) {
        OrderContext context = map.get(orderId);
        StateMachine<OrderState, OrderEvent, OrderContext> stateMachine = stateMachineFactory.getStateMachine(StateMachineOrderStateService.class);
        return stateMachine.sendEvent(orderId, OrderEvent.CONFIRM, context);
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public OrderState cancelOrder(@PathVariable String orderId) {
        OrderContext context = map.get(orderId);
        StateMachine<OrderState, OrderEvent, OrderContext> stateMachine = stateMachineFactory.getStateMachine(StateMachineOrderStateService.class);
        return stateMachine.sendEvent(orderId, OrderEvent.CANCEL, context);
    }

    /**
     * 查询订单状态
     */
    @GetMapping("/{orderId}/state")
    public OrderState getOrderState(@PathVariable String orderId) {
        OrderContext context = map.get(orderId);
        StateMachine<OrderState, OrderEvent, OrderContext> stateMachine = stateMachineFactory.getStateMachine(StateMachineOrderStateService.class);
        return stateMachine.getCurrentState("orderStateMachine", context);
    }

    /**
     * 结束订单
     */
    @GetMapping("/{orderId}/stop")
    public void stop(@PathVariable String orderId) {
        OrderContext context = map.get(orderId);
        StateMachine<OrderState, OrderEvent, OrderContext> stateMachine = stateMachineFactory.getStateMachine(StateMachineOrderStateService.class);
        stateMachine.stop("orderStateMachine", context);
        map.remove(orderId);
    }
} 