package io.github.kk01001.examples.strategy.order;

import io.github.kk01001.design.pattern.strategy.StrategyFactory;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private StrategyFactory strategyFactory;

    @PostMapping("/status")
    public OrderResult updateStatus(@RequestBody OrderParam param, @RequestParam String status) {
        return strategyFactory.execute(OrderStatusEnum.class, status, param);
    }
}