package io.github.archer099.sensitive.core;

import java.util.List;
import java.util.Set;

/**
 * @author archer099
 * @date 2026-01-18 13:02:31
 * @description 敏感词过滤器接口
 */
public interface SensitiveWordFilter {
    
    /**
     * 添加敏感词
     *
     * @param word 敏感词
     */
    void addWord(String word);
    
    /**
     * 添加敏感词（带分类）
     *
     * @param word     敏感词
     * @param category 分类
     */
    void addWord(String word, String category);
    
    /**
     * 批量添加敏感词
     *
     * @param words 敏感词集合
     */
    void addWords(Set<String> words);
    
    /**
     * 批量添加敏感词（带分类）
     *
     * @param words    敏感词集合
     * @param category 分类
     */
    void addWords(Set<String> words, String category);
    
    /**
     * 移除敏感词
     *
     * @param word 敏感词
     */
    void removeWord(String word);
    
    /**
     * 批量移除敏感词
     *
     * @param words 敏感词集合
     */
    void removeWords(Set<String> words);
    
    /**
     * 清空所有敏感词
     */
    void clear();
    
    /**
     * 获取敏感词数量
     *
     * @return 敏感词数量
     */
    int size();
    
    /**
     * 检测是否包含敏感词
     *
     * @param text 待检测文本
     * @return 是否包含敏感词
     */
    boolean contains(String text);
    
    /**
     * 检测是否包含敏感词
     *
     * @param text      待检测文本
     * @param matchType 匹配类型
     * @return 是否包含敏感词
     */
    boolean contains(String text, MatchType matchType);
    
    /**
     * 查找第一个敏感词
     *
     * @param text 待检测文本
     * @return 敏感词结果，未找到返回 null
     */
    SensitiveWordResult findFirst(String text);
    
    /**
     * 查找第一个敏感词
     *
     * @param text      待检测文本
     * @param matchType 匹配类型
     * @return 敏感词结果，未找到返回 null
     */
    SensitiveWordResult findFirst(String text, MatchType matchType);
    
    /**
     * 查找所有敏感词
     *
     * @param text 待检测文本
     * @return 敏感词结果列表
     */
    List<SensitiveWordResult> findAll(String text);
    
    /**
     * 查找所有敏感词
     *
     * @param text      待检测文本
     * @param matchType 匹配类型
     * @return 敏感词结果列表
     */
    List<SensitiveWordResult> findAll(String text, MatchType matchType);
    
    /**
     * 替换敏感词
     *
     * @param text        待处理文本
     * @param replacement 替换字符
     * @return 处理后的文本
     */
    String replace(String text, char replacement);
    
    /**
     * 替换敏感词
     *
     * @param text        待处理文本
     * @param replacement 替换字符
     * @param matchType   匹配类型
     * @return 处理后的文本
     */
    String replace(String text, char replacement, MatchType matchType);
    
    /**
     * 替换敏感词（使用替换字符串）
     *
     * @param text        待处理文本
     * @param replacement 替换字符串
     * @return 处理后的文本
     */
    String replace(String text, String replacement);
    
    /**
     * 高亮敏感词
     *
     * @param text     待处理文本
     * @param startTag 开始标签
     * @param endTag   结束标签
     * @return 处理后的文本
     */
    String highlight(String text, String startTag, String endTag);
    
    /**
     * 高亮敏感词
     *
     * @param text      待处理文本
     * @param startTag  开始标签
     * @param endTag    结束标签
     * @param matchType 匹配类型
     * @return 处理后的文本
     */
    String highlight(String text, String startTag, String endTag, MatchType matchType);
}
