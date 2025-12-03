package io.github.kk01001.business.feign;

import io.github.kk01001.seata.common.result.Result;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * @author kk01001
 * @date 2025-01-08 16:21:00
 * @description
 */
@HttpExchange(value = "order-service")
public interface OrderClient {
    
    /**
     * 创建订单
     *
     * @param userId        用户ID
     * @param commodityCode 商品编码
     * @param count         数量
     * @return 结果
     */
    @PostExchange("/order/create")
    Result<Void> create(@RequestParam("userId") String userId,
                        @RequestParam("commodityCode") String commodityCode,
                        @RequestParam("count") Integer count);
}