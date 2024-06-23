package io.github.kk01001.xxljob.enums;

/**
 * @author linshiqiang
 * date 2023-02-02 14:03
 * 阻塞处理策略枚举
 */
public enum ExecutorBlockStrategyEnum {

    /**
     * 单机窜行
     */
    SERIAL_EXECUTION,

    /**
     * 丢弃后续调度
     */
    DISCARD_LATER,

    /**
     * 覆盖之前调度
     */
    COVER_EARLY;
}
