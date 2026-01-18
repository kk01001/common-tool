package io.github.kk01001.sensitive.core;

/**
 * @author kk01001
 * @date 2026-01-18 13:02:31
 * @description 敏感词匹配类型枚举
 */
public enum MatchType {
    
    /**
     * 最小匹配规则
     * 如：敏感词库["中国", "中国人"]，文本"中国人"，匹配结果为"中国"
     */
    MIN_MATCH,
    
    /**
     * 最大匹配规则
     * 如：敏感词库["中国", "中国人"]，文本"中国人"，匹配结果为"中国人"
     */
    MAX_MATCH
}
