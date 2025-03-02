package io.github.kk01001.examples.strategy.pay;

import io.github.kk01001.strategy.StrategyFactory;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/pay")
public class PayController {

    @Resource
    private StrategyFactory strategyFactory;

    @PostMapping
    public PayResult pay(@RequestBody PayParam param, String payType) {
        return strategyFactory.execute(PayTypeEnum.class, payType, param);
    }
}