package io.github.kk01001.business.service;

/**
 * @author kk01001
 * @date 2025-01-08 16:22:00
 * @description
 */
public interface BusinessService {
    /**
     * 采购
     *
     * @param userId        用户ID
     * @param commodityCode 商品编码
     * @param count         数量
     */
    void purchase(String userId, String commodityCode, int count);
}