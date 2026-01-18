package io.github.kk01001.sensitive.example.dto;

import io.github.kk01001.sensitive.annotation.SensitiveWordField;
import io.github.kk01001.sensitive.core.HandleType;
import lombok.Data;

/**
 * @author kk01001
 * @date 2026-01-18 13:02:31
 * @description 内容检测请求
 */
@Data
public class ContentCheckRequest {

    /**
     * 标题
     */
    @SensitiveWordField(handleType = HandleType.REPLACE)
    private String title;

    /**
     * 内容
     */
    @SensitiveWordField(handleType = HandleType.REPLACE)
    private String content;

    /**
     * 作者
     */
    private String author;
}
