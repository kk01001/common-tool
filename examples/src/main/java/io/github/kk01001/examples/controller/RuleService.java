package io.github.kk01001.examples.controller;

import io.github.kk01001.examples.model.DataModel;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @date 2024-09-07 22:33:00
 * @description
 */
@Component
public class RuleService {

    public Integer getTokenRate(DataModel dataModel) {
        // 基于 dataModel 动态生成限流规则
        return 1;
    }
}
