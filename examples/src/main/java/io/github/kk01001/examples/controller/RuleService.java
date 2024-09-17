package io.github.kk01001.examples.controller;

import io.github.kk01001.examples.model.DataModel;
import io.github.kk01001.ratelimter.enums.RateLimiterType;
import io.github.kk01001.ratelimter.model.FlowRule;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @date 2024-09-07 22:33:00
 * @description
 */
@Component("ruleService")
public class RuleService {

    public FlowRule getRule(DataModel dataModel) {

        FlowRule flowRule = new FlowRule();
        flowRule.setRateLimiterType(RateLimiterType.REDISSON);
        flowRule.setKey(String.format("key002:%s", dataModel.getCode()));
        flowRule.setMaxRequests(10);
        flowRule.setWindowTime(1);
        return flowRule;
    }

    public String getKey(DataModel dataModel) {
        // 基于 dataModel 动态生成限流规则
        return String.format("key001:%s", dataModel.getCode());
    }

    public Integer getBucketCapacity(DataModel dataModel) {
        // 基于 dataModel 动态生成限流规则
        return 20;
    }


    public Integer getTokenRate(DataModel dataModel) {
        // 基于 dataModel 动态生成限流规则
        return 10;
    }

    public Integer getMaxRequests(DataModel dataModel) {
        // 基于 dataModel 动态生成限流规则
        return 1;
    }
}
