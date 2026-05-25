package io.github.archer099.business.service;

/**
 * @author archer099
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