package io.github.archer099.redisson.properties;

import io.github.archer099.redisson.enums.DelayStrategyType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author archer099
 * @date 2026-01-15 15:30:00
 * @description 重试延迟策略配置
 */
@Getter
@Setter
public class RetryDelayProperties {

    /**
     * 延迟策略类型
     * CONSTANT: 固定延迟
     * EQUAL_JITTER: 适度随机延迟（默认）
     * FULL_JITTER: 完全随机延迟
     * DECORRELATED_JITTER: 去相关抖动延迟
     */
    private DelayStrategyType type = DelayStrategyType.EQUAL_JITTER;

    /**
     * 最小延迟时间（毫秒）
     * 对于 CONSTANT 类型，此值即为固定延迟时间
     */
    private long minDelay = 1000;

    /**
     * 最大延迟时间（毫秒）
     * 对于 CONSTANT 类型，此值无效
     */
    private long maxDelay = 2000;
}
