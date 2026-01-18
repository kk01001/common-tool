package io.github.kk01001.sensitive.handler;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import io.github.kk01001.sensitive.core.*;
import io.github.kk01001.sensitive.properties.SensitiveWordProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author kk01001
 * @date 2026-01-18 13:02:31
 * @description 敏感词服务
 */
@Slf4j
public class SensitiveWordService implements InitializingBean {
    
    @Getter
    private final SensitiveWordFilter filter;
    private final SensitiveWordProperties properties;
    
    /**
     * 白名单过滤器
     */
    private final Set<String> whiteList = new HashSet<>();
    
    public SensitiveWordService(SensitiveWordFilter filter, SensitiveWordProperties properties) {
        this.filter = filter;
        this.properties = properties;
    }
    
    @Override
    public void afterPropertiesSet() {
        // 初始化白名单
        whiteList.addAll(properties.getWhiteList());
        
        // 加载直接配置的敏感词
        if (!properties.getWords().isEmpty()) {
            filter.addWords(properties.getWords());
            log.info("从配置加载 {} 个敏感词", properties.getWords().size());
        }
        
        // 加载内置词库
        for (String dictPath : properties.getDictPaths()) {
            loadFromClasspath(dictPath);
        }
        
        // 加载外部词库
        for (String dictPath : properties.getExternalDictPaths()) {
            loadFromFile(dictPath);
        }
        
        log.info("敏感词库初始化完成，共 {} 个敏感词", filter.size());
    }
    
    /**
     * 从 classpath 加载词库
     */
    public void loadFromClasspath(String path) {
        try {
            InputStream is = ResourceUtil.getStream(path);
            if (is == null) {
                log.warn("敏感词库文件不存在: {}", path);
                return;
            }
            
            Set<String> words = loadWordsFromStream(is);
            filter.addWords(words);
            log.info("从 {} 加载 {} 个敏感词", path, words.size());
        } catch (Exception e) {
            log.error("加载敏感词库失败: {}", path, e);
        }
    }
    
    /**
     * 从文件系统加载词库
     */
    public void loadFromFile(String path) {
        try {
            Path filePath = Path.of(path);
            if (!Files.exists(filePath)) {
                log.warn("敏感词库文件不存在: {}", path);
                return;
            }
            
            try (InputStream is = Files.newInputStream(filePath)) {
                Set<String> words = loadWordsFromStream(is);
                filter.addWords(words);
                log.info("从 {} 加载 {} 个敏感词", path, words.size());
            }
        } catch (Exception e) {
            log.error("加载敏感词库失败: {}", path, e);
        }
    }
    
    /**
     * 从输入流加载词汇
     */
    private Set<String> loadWordsFromStream(InputStream is) throws IOException {
        Set<String> words = new HashSet<>();
        try (BufferedReader reader = IoUtil.getReader(is, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 跳过空行和注释行
                if (StrUtil.isNotBlank(line) && !line.startsWith("#")) {
                    words.add(line);
                }
            }
        }
        return words;
    }
    
    /**
     * 检测是否包含敏感词（考虑白名单）
     */
    public boolean contains(String text) {
        return contains(text, properties.getMatchType());
    }
    
    /**
     * 检测是否包含敏感词（考虑白名单）
     */
    public boolean contains(String text, MatchType matchType) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        
        // 先检查白名单
        String processedText = removeWhiteListWords(text);
        return filter.contains(processedText, matchType);
    }
    
    /**
     * 查找第一个敏感词
     */
    public SensitiveWordResult findFirst(String text) {
        return findFirst(text, properties.getMatchType());
    }
    
    /**
     * 查找第一个敏感词
     */
    public SensitiveWordResult findFirst(String text, MatchType matchType) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        
        String processedText = removeWhiteListWords(text);
        return filter.findFirst(processedText, matchType);
    }
    
    /**
     * 查找所有敏感词
     */
    public List<SensitiveWordResult> findAll(String text) {
        return findAll(text, properties.getMatchType());
    }
    
    /**
     * 查找所有敏感词
     */
    public List<SensitiveWordResult> findAll(String text, MatchType matchType) {
        if (StrUtil.isBlank(text)) {
            return List.of();
        }
        
        String processedText = removeWhiteListWords(text);
        return filter.findAll(processedText, matchType);
    }
    
    /**
     * 替换敏感词
     */
    public String replace(String text) {
        return replace(text, properties.getReplaceChar());
    }
    
    /**
     * 替换敏感词
     */
    public String replace(String text, char replacement) {
        return replace(text, replacement, properties.getMatchType());
    }
    
    /**
     * 替换敏感词
     */
    public String replace(String text, char replacement, MatchType matchType) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        
        return filter.replace(text, replacement, matchType);
    }
    
    /**
     * 替换敏感词（使用配置的替换字符串）
     */
    public String replaceWithStr(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        
        String replaceStr = properties.getReplaceStr();
        if (StrUtil.isNotBlank(replaceStr)) {
            return filter.replace(text, replaceStr);
        }
        
        return replace(text);
    }
    
    /**
     * 高亮敏感词
     */
    public String highlight(String text) {
        return highlight(text, properties.getHighlightStartTag(), properties.getHighlightEndTag());
    }
    
    /**
     * 高亮敏感词
     */
    public String highlight(String text, String startTag, String endTag) {
        return highlight(text, startTag, endTag, properties.getMatchType());
    }
    
    /**
     * 高亮敏感词
     */
    public String highlight(String text, String startTag, String endTag, MatchType matchType) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        
        return filter.highlight(text, startTag, endTag, matchType);
    }
    
    /**
     * 处理文本（根据配置的处理类型）
     */
    public String process(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        
        return switch (properties.getHandleType()) {
            case REPLACE -> replace(text);
            case EXCEPTION -> {
                List<SensitiveWordResult> results = findAll(text);
                if (!results.isEmpty()) {
                    String message = StrUtil.format(properties.getExceptionMessage(), 
                            results.stream().map(SensitiveWordResult::getWord).toList());
                    throw new SensitiveWordException(message, results);
                }
                yield text;
            }
            case HIGHLIGHT -> highlight(text);
            case DETECT_ONLY -> {
                List<SensitiveWordResult> results = findAll(text);
                if (!results.isEmpty()) {
                    log.warn("检测到敏感词: {}", results.stream().map(SensitiveWordResult::getWord).toList());
                }
                yield text;
            }
        };
    }
    
    /**
     * 添加敏感词
     */
    public void addWord(String word) {
        filter.addWord(word);
    }
    
    /**
     * 添加敏感词（带分类）
     */
    public void addWord(String word, String category) {
        filter.addWord(word, category);
    }
    
    /**
     * 批量添加敏感词
     */
    public void addWords(Set<String> words) {
        filter.addWords(words);
    }
    
    /**
     * 移除敏感词
     */
    public void removeWord(String word) {
        filter.removeWord(word);
    }
    
    /**
     * 添加白名单词汇
     */
    public void addWhiteList(String word) {
        whiteList.add(word);
    }
    
    /**
     * 批量添加白名单词汇
     */
    public void addWhiteList(Set<String> words) {
        whiteList.addAll(words);
    }
    
    /**
     * 移除白名单中的词汇（临时处理）
     */
    private String removeWhiteListWords(String text) {
        if (whiteList.isEmpty()) {
            return text;
        }
        
        String result = text;
        for (String word : whiteList) {
            // 使用特殊字符临时替换白名单词汇
            result = result.replace(word, "\u0000".repeat(word.length()));
        }
        return result;
    }
    
    /**
     * 获取敏感词数量
     */
    public int size() {
        return filter.size();
    }
}
