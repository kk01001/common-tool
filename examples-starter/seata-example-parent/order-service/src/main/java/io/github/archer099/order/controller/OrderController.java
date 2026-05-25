package io.github.archer099.order.controller;

import io.github.archer099.order.service.OrderService;
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
 * @date 2025-01-08 16:05:00
 * @description
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<Void> create(@RequestParam("userId") String userId,
                               @RequestParam("commodityCode") String commodityCode,
                               @RequestParam("count") Integer count) {
        orderService.create(userId, commodityCode, count);
        return Result.success();
    }
}