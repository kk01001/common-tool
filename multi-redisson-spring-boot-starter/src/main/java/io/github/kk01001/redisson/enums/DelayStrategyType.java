package io.github.archer099.redisson.enums;

/**
 * @author archer099
 * @date 2026-01-15 15:30:00
 * @description 延迟策略类型
 */
public enum DelayStrategyType {

    /**
     * 固定延迟
     * 每次重试使用相同的延迟时间
     */
    CONSTANT,

    /**
     * 适度随机延迟（默认）
     * 在指数退避的基础上引入适度随机性，同时保持延迟值的稳定性
     */
    EQUAL_JITTER,

    /**
     * 完全随机延迟
     * 对指数退避延迟应用完全随机化
     */
    FULL_JITTER,

    /**
     * 去相关抖动延迟
     * 指数增长延迟，同时引入受前次退避时长影响的随机性
     */
    DECORRELATED_JITTER
}
