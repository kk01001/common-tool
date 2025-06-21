package io.github.kk01001.dynamic.mq.enums;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 消息选择器类型枚举
 */
public enum MessageSelectorType {
    
    /**
     * 未设置，使用默认配置
     */
    UNSET,
    
    /**
     * 按标签过滤
     */
    TAG,
    
    /**
     * 按SQL92表达式过滤
     */
    SQL92
}
