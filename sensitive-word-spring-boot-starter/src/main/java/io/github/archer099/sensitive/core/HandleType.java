package io.github.archer099.sensitive.core;

/**
 * @author archer099
 * @date 2026-01-18 13:02:31
 * @description 敏感词处理类型枚举
 */
public enum HandleType {
    
    /**
     * 替换为指定字符
     */
    REPLACE,
    
    /**
     * 抛出异常
     */
    EXCEPTION,
    
    /**
     * 高亮标记（HTML标签包裹）
     */
    HIGHLIGHT,
    
    /**
     * 仅检测不处理
     */
    DETECT_ONLY
}
