package io.github.archer099.sensitive.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author archer099
 * @date 2026-01-18 13:02:31
 * @description 敏感词检测结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordResult {
    
    /**
     * 敏感词
     */
    private String word;
    
    /**
     * 开始位置
     */
    private int startIndex;
    
    /**
     * 结束位置
     */
    private int endIndex;
    
    /**
     * 敏感词类型/分类
     */
    private String category;
}
