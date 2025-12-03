package io.github.kk01001.storage.service;

public interface StorageService {
    /**
     * 扣减库存
     *
     * @param commodityCode 商品编码
     * @param count         扣减数量
     */
    void deduct(String commodityCode, int count);
}
