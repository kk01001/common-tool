package io.github.kk01001.examples.strategy.order;

import io.github.kk01001.strategy.StrategyFactory;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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