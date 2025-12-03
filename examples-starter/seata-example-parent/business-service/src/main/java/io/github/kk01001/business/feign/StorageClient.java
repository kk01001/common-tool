package io.github.kk01001.business.feign;

import io.github.kk01001.seata.common.result.Result;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * @author kk01001
 * @date 2025-01-08 16:20:00
 * @description
 */
@HttpExchange(value = "storage-service")
public interface StorageClient {
    
    /**
     * 扣减库存
     *
     * @param commodityCode 商品编码
     * @param count         数量
     * @return 结果
     */
    @PostExchange("/storage/deduct")
    Result<Void> deduct(@RequestParam("commodityCode") String commodityCode, @RequestParam("count") Integer count);
}