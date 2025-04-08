package io.github.kk01001.examples.design.statemachine;

import io.github.kk01001.design.pattern.statemachine.guard.StateTransitionGuard;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 订单取消时间守卫条件，限制在订单创建后30分钟内才能取消
 */
@Component
public class OrderCancelTimeGuard implements StateTransitionGuard<OrderState, OrderEvent, OrderContext> {

    // 记录订单创建时间
    private final Map<String, LocalDateTime> orderCreateTimeMap = new ConcurrentHashMap<>();

    // 允许取消的最长时间（分钟）
    private static final long MAX_CANCEL_TIME_MINUTES = 30;

    /**
     * 记录订单创建时间
     */
    public void recordOrderCreateTime(String orderId) {
        orderCreateTimeMap.putIfAbsent(orderId, LocalDateTime.now());
    }

    @Override
    public boolean evaluate(OrderState sourceState, OrderEvent event, OrderContext context) {
        // 只针对取消事件进行检查
        if (event == OrderEvent.CANCEL) {
            String orderId = context.getOrderId();

            // 如果没有记录创建时间，先记录（模拟测试用）
            if (!orderCreateTimeMap.containsKey(orderId)) {
                recordOrderCreateTime(orderId);
                return true;
            }

            LocalDateTime createTime = orderCreateTimeMap.get(orderId);
            LocalDateTime now = LocalDateTime.now();

            // 计算时间差
            Duration duration = Duration.between(createTime, now);
            long minutesPassed = duration.toMinutes();

            // 判断是否在允许的时间范围内
            return minutesPassed <= MAX_CANCEL_TIME_MINUTES;
        }

        // 其他事件不限制
        return true;
    }

    @Override
    public String getRejectionReason() {
        return "订单创建超过" + MAX_CANCEL_TIME_MINUTES + "分钟，不能取消";
    }
} 