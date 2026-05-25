package io.github.archer099.business.controller;

import io.github.archer099.business.service.BusinessService;
import io.github.archer099.seata.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author archer099
 * @date 2025-01-08 16:30:00
 * @description
 */
@Tag(name = "业务管理")
@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @Operation(summary = "采购")
    @PostMapping("/purchase")
    public Result<Void> purchase(@RequestParam("userId") String userId,
                                 @RequestParam("commodityCode") String commodityCode,
                                 @RequestParam("count") Integer count) {
        businessService.purchase(userId, commodityCode, count);
        return Result.success();
    }
}