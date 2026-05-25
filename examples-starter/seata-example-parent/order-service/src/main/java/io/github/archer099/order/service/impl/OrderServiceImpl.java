package io.github.archer099.order.service.impl;

import io.github.archer099.order.entity.Order;
import io.github.archer099.order.feign.AccountClient;
import io.github.archer099.order.mapper.OrderMapper;
import io.github.archer099.order.service.OrderService;
import io.seata.core.context.RootContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author archer099
 * @date 2025-01-08 16:00:00
 * @description
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final AccountClient accountClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(String userId, String commodityCode, int count) {
        log.info("Order Service Begin ... xid: " + RootContext.getXID());

        // 计算订单金额
        int orderMoney = count * 5;

        // 调用账户余额扣减
        log.info("Order Service calling account service ... ");
        accountClient.debit(userId, orderMoney);

        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setCommodityCode(commodityCode);
        order.setCount(count);
        order.setMoney(orderMoney);

        orderMapper.insert(order);

        log.info("Order Service End ... Created " + order);
    }
}
