package io.github.archer099.sensitive.example.service;

import io.github.archer099.sensitive.core.MatchType;
import io.github.archer099.sensitive.core.SensitiveWordResult;
import io.github.archer099.sensitive.example.dto.ContentCheckResponse;
import io.github.archer099.sensitive.handler.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @author archer099
 * @date 2026-01-18 13:02:31
 * @description 敏感词过滤示例服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordDemoService {

    private final SensitiveWordService sensitiveWordService;

    /**
     * 检测文本
     */
    public ContentCheckResponse checkText(String text) {
        List<SensitiveWordResult> results = sensitiveWordService.findAll(text);
        
        return ContentCheckResponse.builder()
                .containsSensitiveWord(!results.isEmpty())
                .sensitiveWords(results)
                .originalText(text)
                .processedText(text)
                .sensitiveWordCount(results.size())
                .build();
    }

    /**
     * 替换敏感词
     */
    public ContentCheckResponse replaceText(String text, char replaceChar) {
        List<SensitiveWordResult> results = sensitiveWordService.findAll(text);
        String processedText = sensitiveWordService.replace(text, replaceChar);
        
        return ContentCheckResponse.builder()
                .containsSensitiveWord(!results.isEmpty())
                .sensitiveWords(results)
                .originalText(text)
                .processedText(processedText)
                .sensitiveWordCount(results.size())
                .build();
    }

    /**
     * 高亮敏感词
     */
    public ContentCheckResponse highlightText(String text, String startTag, String endTag) {
        List<SensitiveWordResult> results = sensitiveWordService.findAll(text);
        String processedText = sensitiveWordService.highlight(text, startTag, endTag);
        
        return ContentCheckResponse.builder()
                .containsSensitiveWord(!results.isEmpty())
                .sensitiveWords(results)
                .originalText(text)
                .processedText(processedText)
                .sensitiveWordCount(results.size())
                .build();
    }

    /**
     * 查找所有敏感词
     */
    public List<SensitiveWordResult> findAllSensitiveWords(String text, MatchType matchType) {
        return sensitiveWordService.findAll(text, matchType);
    }

    /**
     * 添加敏感词
     */
    public void addSensitiveWord(String word, String category) {
        sensitiveWordService.addWord(word, category);
        log.info("添加敏感词: {}, 分类: {}", word, category);
    }

    /**
     * 批量添加敏感词
     */
    public void addSensitiveWords(Set<String> words) {
        sensitiveWordService.addWords(words);
        log.info("批量添加敏感词: {} 个", words.size());
    }

    /**
     * 移除敏感词
     */
    public void removeSensitiveWord(String word) {
        sensitiveWordService.removeWord(word);
        log.info("移除敏感词: {}", word);
    }

    /**
     * 获取敏感词数量
     */
    public int getSensitiveWordCount() {
        return sensitiveWordService.size();
    }

    /**
     * 添加白名单
     */
    public void addWhiteList(String word) {
        sensitiveWordService.addWhiteList(word);
        log.info("添加白名单: {}", word);
    }
}
