package io.github.archer099.sensitive.example.dto;

import io.github.archer099.sensitive.core.SensitiveWordResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author archer099
 * @date 2026-01-18 13:02:31
 * @description 内容检测响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentCheckResponse {

    /**
     * 是否包含敏感词
     */
    private boolean containsSensitiveWord;

    /**
     * 检测到的敏感词列表
     */
    private List<SensitiveWordResult> sensitiveWords;

    /**
     * 原始文本
     */
    private String originalText;

    /**
     * 处理后的文本（替换/高亮后）
     */
    private String processedText;

    /**
     * 敏感词数量
     */
    private int sensitiveWordCount;
}
