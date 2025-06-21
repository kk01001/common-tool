package io.github.kk01001.dynamic.mq.enums;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 消费模式枚举
 */
public enum ConsumeMode {
    
    /**
     * 未设置，使用默认配置
     */
    UNSET,
    
    /**
     * 并发消费模式（不保证顺序）
     */
    CONCURRENTLY,
    
    /**
     * 顺序消费模式（保证顺序）
     */
    ORDERLY
}
