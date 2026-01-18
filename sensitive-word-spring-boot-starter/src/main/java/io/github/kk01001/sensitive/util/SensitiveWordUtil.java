package io.github.kk01001.sensitive.util;

import io.github.kk01001.sensitive.core.DfaSensitiveWordFilter;
import io.github.kk01001.sensitive.core.MatchType;
import io.github.kk01001.sensitive.core.SensitiveWordFilter;
import io.github.kk01001.sensitive.core.SensitiveWordResult;

import java.util.List;
import java.util.Set;

/**
 * @author kk01001
 * @date 2026-01-18 13:02:31
 * @description 敏感词工具类，提供静态方法，方便快速使用
 */
public final class SensitiveWordUtil {
    
    private static volatile SensitiveWordFilter defaultFilter;
    
    private SensitiveWordUtil() {
    }
    
    /**
     * 获取默认过滤器
     */
    public static SensitiveWordFilter getFilter() {
        if (defaultFilter == null) {
            synchronized (SensitiveWordUtil.class) {
                if (defaultFilter == null) {
                    defaultFilter = new DfaSensitiveWordFilter();
                }
            }
        }
        return defaultFilter;
    }
    
    /**
     * 设置默认过滤器
     */
    public static void setFilter(SensitiveWordFilter filter) {
        defaultFilter = filter;
    }
    
    /**
     * 添加敏感词
     */
    public static void addWord(String word) {
        getFilter().addWord(word);
    }
    
    /**
     * 添加敏感词（带分类）
     */
    public static void addWord(String word, String category) {
        getFilter().addWord(word, category);
    }
    
    /**
     * 批量添加敏感词
     */
    public static void addWords(Set<String> words) {
        getFilter().addWords(words);
    }
    
    /**
     * 检测是否包含敏感词
     */
    public static boolean contains(String text) {
        return getFilter().contains(text);
    }
    
    /**
     * 检测是否包含敏感词
     */
    public static boolean contains(String text, MatchType matchType) {
        return getFilter().contains(text, matchType);
    }
    
    /**
     * 查找第一个敏感词
     */
    public static SensitiveWordResult findFirst(String text) {
        return getFilter().findFirst(text);
    }
    
    /**
     * 查找所有敏感词
     */
    public static List<SensitiveWordResult> findAll(String text) {
        return getFilter().findAll(text);
    }
    
    /**
     * 替换敏感词
     */
    public static String replace(String text, char replacement) {
        return getFilter().replace(text, replacement);
    }
    
    /**
     * 替换敏感词
     */
    public static String replace(String text, String replacement) {
        return getFilter().replace(text, replacement);
    }
    
    /**
     * 高亮敏感词
     */
    public static String highlight(String text, String startTag, String endTag) {
        return getFilter().highlight(text, startTag, endTag);
    }
}
