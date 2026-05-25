package io.github.archer099.business.service.impl;

import io.github.archer099.business.feign.OrderClient;
import io.github.archer099.business.feign.StorageClient;
import io.github.archer099.business.service.BusinessService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author archer099
 * @date 2025-01-08 16:25:00
 * @description
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final StorageClient storageClient;
    private final OrderClient orderClient;

    @Override
    @GlobalTransactional(name = "my_test_tx_group", rollbackFor = Exception.class)
    public void purchase(String userId, String commodityCode, int count) {
        log.info("Business Service Begin ... xid: " + RootContext.getXID());
        storageClient.deduct(commodityCode, count);
        orderClient.create(userId, commodityCode, count);
    }
}