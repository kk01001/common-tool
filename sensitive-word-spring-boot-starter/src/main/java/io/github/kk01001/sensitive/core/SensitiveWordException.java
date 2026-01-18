package io.github.kk01001.sensitive.core;

import java.util.List;

/**
 * @author kk01001
 * @date 2026-01-18 13:02:31
 * @description 敏感词异常
 */
public class SensitiveWordException extends RuntimeException {
    
    /**
     * 检测到的敏感词列表
     */
    private final List<SensitiveWordResult> sensitiveWords;
    
    public SensitiveWordException(String message, List<SensitiveWordResult> sensitiveWords) {
        super(message);
        this.sensitiveWords = sensitiveWords;
    }
    
    public SensitiveWordException(String message, Throwable cause, List<SensitiveWordResult> sensitiveWords) {
        super(message, cause);
        this.sensitiveWords = sensitiveWords;
    }
    
    public List<SensitiveWordResult> getSensitiveWords() {
        return sensitiveWords;
    }
    
    /**
     * 获取第一个敏感词
     *
     * @return 第一个敏感词
     */
    public String getFirstSensitiveWord() {
        if (sensitiveWords != null && !sensitiveWords.isEmpty()) {
            return sensitiveWords.get(0).getWord();
        }
        return null;
    }
    
    /**
     * 获取所有敏感词
     *
     * @return 敏感词列表
     */
    public List<String> getAllSensitiveWords() {
        if (sensitiveWords == null) {
            return List.of();
        }
        return sensitiveWords.stream()
                .map(SensitiveWordResult::getWord)
                .toList();
    }
}
