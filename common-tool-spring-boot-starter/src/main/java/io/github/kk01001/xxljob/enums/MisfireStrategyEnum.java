package io.github.kk01001.xxljob.enums;

/**
 * @author kk01001
 * date 2023-02-02 14:03
 * 调度过期策略枚举
 */
public enum MisfireStrategyEnum {

    /**
     * 忽略
     */
    DO_NOTHING,

    /**
     * 立即执行一次
     */
    FIRE_ONCE_NOW;

}
