package io.github.kk01001.order.service;

import io.github.kk01001.order.entity.Order;

public interface OrderService {
    /**
     * 创建订单
     *
     * @param userId        用户ID
     * @param commodityCode 商品编码
     * @param count         数量
     */
    void create(String userId, String commodityCode, int count);
}
