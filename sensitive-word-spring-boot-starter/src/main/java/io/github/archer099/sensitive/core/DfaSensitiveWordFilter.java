package io.github.archer099.sensitive.core;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author archer099
 * @date 2026-01-18 13:02:31
 * @description 基于 DFA（确定性有限自动机）算法的敏感词过滤器实现，时间复杂度 O(n)，空间换时间，适合大量文本检测
 */
@Slf4j
public class DfaSensitiveWordFilter implements SensitiveWordFilter {
    
    /**
     * 根节点
     */
    private final DfaNode root = new DfaNode();
    
    /**
     * 敏感词数量
     */
    private final AtomicInteger wordCount = new AtomicInteger(0);
    
    /**
     * 读写锁，保证线程安全
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * 是否忽略大小写
     */
    private boolean ignoreCase = true;
    
    /**
     * 是否跳过空白字符
     */
    private boolean skipWhitespace = true;
    
    /**
     * 需要跳过的字符集合（如特殊符号）
     */
    private Set<Character> skipChars = new HashSet<>();
    
    public DfaSensitiveWordFilter() {
    }
    
    public DfaSensitiveWordFilter(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    
    public void setSkipWhitespace(boolean skipWhitespace) {
        this.skipWhitespace = skipWhitespace;
    }
    
    public void setSkipChars(Set<Character> skipChars) {
        this.skipChars = skipChars;
    }
    
    @Override
    public void addWord(String word) {
        addWord(word, null);
    }
    
    @Override
    public void addWord(String word, String category) {
        if (StrUtil.isBlank(word)) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            String processedWord = ignoreCase ? word.toLowerCase() : word;
            DfaNode current = root;
            
            for (int i = 0; i < processedWord.length(); i++) {
                char c = processedWord.charAt(i);
                DfaNode child = current.getChild(c);
                
                if (child == null) {
                    child = new DfaNode();
                    current.addChild(c, child);
                }
                current = child;
            }
            
            // 标记为结束节点
            if (!current.isEnd()) {
                current.setEnd(true);
                current.setWord(word);
                current.setCategory(category);
                wordCount.incrementAndGet();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void addWords(Set<String> words) {
        addWords(words, null);
    }
    
    @Override
    public void addWords(Set<String> words, String category) {
        if (words == null || words.isEmpty()) {
            return;
        }
        
        for (String word : words) {
            addWord(word, category);
        }
        
        log.info("成功添加 {} 个敏感词，分类：{}", words.size(), category);
    }
    
    @Override
    public void removeWord(String word) {
        if (StrUtil.isBlank(word)) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            String processedWord = ignoreCase ? word.toLowerCase() : word;
            DfaNode current = root;
            
            // 记录路径
            List<DfaNode> path = new ArrayList<>();
            List<Character> chars = new ArrayList<>();
            
            for (int i = 0; i < processedWord.length(); i++) {
                char c = processedWord.charAt(i);
                path.add(current);
                chars.add(c);
                
                DfaNode child = current.getChild(c);
                if (child == null) {
                    return; // 词不存在
                }
                current = child;
            }
            
            if (current.isEnd()) {
                current.setEnd(false);
                current.setWord(null);
                current.setCategory(null);
                wordCount.decrementAndGet();
                
                // 清理无用节点
                for (int i = path.size() - 1; i >= 0; i--) {
                    DfaNode node = path.get(i);
                    char c = chars.get(i);
                    DfaNode child = node.getChild(c);
                    
                    if (child != null && !child.isEnd() && child.getChildren().isEmpty()) {
                        node.getChildren().remove(c);
                    } else {
                        break;
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void removeWords(Set<String> words) {
        if (words == null || words.isEmpty()) {
            return;
        }
        
        for (String word : words) {
            removeWord(word);
        }
    }
    
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            root.getChildren().clear();
            wordCount.set(0);
            log.info("已清空所有敏感词");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public int size() {
        return wordCount.get();
    }
    
    @Override
    public boolean contains(String text) {
        return contains(text, MatchType.MIN_MATCH);
    }
    
    @Override
    public boolean contains(String text, MatchType matchType) {
        return findFirst(text, matchType) != null;
    }
    
    @Override
    public SensitiveWordResult findFirst(String text) {
        return findFirst(text, MatchType.MIN_MATCH);
    }
    
    @Override
    public SensitiveWordResult findFirst(String text, MatchType matchType) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        
        lock.readLock().lock();
        try {
            String processedText = ignoreCase ? text.toLowerCase() : text;
            
            for (int i = 0; i < processedText.length(); i++) {
                SensitiveWordResult result = checkSensitiveWord(processedText, i, matchType);
                if (result != null) {
                    return result;
                }
            }
            
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public List<SensitiveWordResult> findAll(String text) {
        return findAll(text, MatchType.MIN_MATCH);
    }
    
    @Override
    public List<SensitiveWordResult> findAll(String text, MatchType matchType) {
        List<SensitiveWordResult> results = new ArrayList<>();
        
        if (StrUtil.isBlank(text)) {
            return results;
        }
        
        lock.readLock().lock();
        try {
            String processedText = ignoreCase ? text.toLowerCase() : text;
            
            int i = 0;
            while (i < processedText.length()) {
                SensitiveWordResult result = checkSensitiveWord(processedText, i, matchType);
                if (result != null) {
                    results.add(result);
                    i = result.getEndIndex() + 1;
                } else {
                    i++;
                }
            }
            
            return results;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public String replace(String text, char replacement) {
        return replace(text, replacement, MatchType.MIN_MATCH);
    }
    
    @Override
    public String replace(String text, char replacement, MatchType matchType) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        
        List<SensitiveWordResult> results = findAll(text, matchType);
        if (results.isEmpty()) {
            return text;
        }
        
        StringBuilder sb = new StringBuilder(text);
        // 从后往前替换，避免索引变化
        for (int i = results.size() - 1; i >= 0; i--) {
            SensitiveWordResult result = results.get(i);
            String replaceStr = StrUtil.repeat(replacement, result.getEndIndex() - result.getStartIndex() + 1);
            sb.replace(result.getStartIndex(), result.getEndIndex() + 1, replaceStr);
        }
        
        return sb.toString();
    }
    
    @Override
    public String replace(String text, String replacement) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        
        List<SensitiveWordResult> results = findAll(text, MatchType.MIN_MATCH);
        if (results.isEmpty()) {
            return text;
        }
        
        StringBuilder sb = new StringBuilder(text);
        // 从后往前替换，避免索引变化
        for (int i = results.size() - 1; i >= 0; i--) {
            SensitiveWordResult result = results.get(i);
            sb.replace(result.getStartIndex(), result.getEndIndex() + 1, replacement);
        }
        
        return sb.toString();
    }
    
    @Override
    public String highlight(String text, String startTag, String endTag) {
        return highlight(text, startTag, endTag, MatchType.MIN_MATCH);
    }
    
    @Override
    public String highlight(String text, String startTag, String endTag, MatchType matchType) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        
        List<SensitiveWordResult> results = findAll(text, matchType);
        if (results.isEmpty()) {
            return text;
        }
        
        StringBuilder sb = new StringBuilder(text);
        // 从后往前插入，避免索引变化
        for (int i = results.size() - 1; i >= 0; i--) {
            SensitiveWordResult result = results.get(i);
            sb.insert(result.getEndIndex() + 1, endTag);
            sb.insert(result.getStartIndex(), startTag);
        }
        
        return sb.toString();
    }
    
    /**
     * 从指定位置开始检查敏感词
     *
     * @param text       文本
     * @param beginIndex 开始位置
     * @param matchType  匹配类型
     * @return 敏感词结果
     */
    private SensitiveWordResult checkSensitiveWord(String text, int beginIndex, MatchType matchType) {
        DfaNode current = root;
        int matchLength = 0;
        SensitiveWordResult lastMatch = null;
        int skipCount = 0;
        
        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // 跳过空白字符
            if (skipWhitespace && Character.isWhitespace(c)) {
                skipCount++;
                continue;
            }
            
            // 跳过特殊字符
            if (skipChars.contains(c)) {
                skipCount++;
                continue;
            }
            
            DfaNode child = current.getChild(c);
            
            if (child == null) {
                break;
            }
            
            current = child;
            matchLength++;
            
            if (current.isEnd()) {
                lastMatch = SensitiveWordResult.builder()
                        .word(current.getWord())
                        .startIndex(beginIndex)
                        .endIndex(beginIndex + matchLength - 1 + skipCount)
                        .category(current.getCategory())
                        .build();
                
                // 最小匹配模式直接返回
                if (matchType == MatchType.MIN_MATCH) {
                    return lastMatch;
                }
            }
        }
        
        // 最大匹配模式返回最后匹配的结果
        return lastMatch;
    }
}
