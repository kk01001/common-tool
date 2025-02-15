/*
 * @Author: linshiqiang
 * @Date: 2025-02-15 15:51:41
 * @Description: Do not edit
 */
package io.github.kk01001.desensitize.handler;

import io.github.kk01001.desensitize.annotation.Desensitize;
import org.springframework.util.StringUtils;

/**
 * @author kk01001
 * @description 抽象脱敏处理器，提供通用的脱敏逻辑
 */
public abstract class AbstractDesensitizeHandler implements DesensitizeHandler {

    public String doDesensitize(String value, int startIndex, int endIndex, String maskChar) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        int length = value.length();

        // 处理负数索引（从末尾开始计算）
        int start = startIndex >= 0 ? startIndex : length + startIndex;
        int end = endIndex >= 0 ? endIndex : length + endIndex;

        // 确保索引在有效范围内
        start = Math.max(0, Math.min(start, length));
        end = Math.max(0, Math.min(end, length));

        // 确保start不大于end
        if (start >= end) {
            return value;
        }

        // 构建脱敏后的字符串

        return value.substring(0, start) +
                maskChar.repeat(end - start) +
                value.substring(end);
    }

    /**
     * 获取注解中的参数并执行脱敏
     * 子类可以选择性重写此方法来实现自定义逻辑
     */
    @Override
    public String desensitize(String value, Desensitize annotation) {
        if (annotation.startIndex() != 0 || annotation.endIndex() != -1
                || !annotation.maskChar().equals("*")) {
            return doDesensitize(value, annotation.startIndex(), annotation.endIndex(), annotation.maskChar());
        }
        return desensitize(value);
    }

    /**
     * 使用默认参数的脱敏方法
     * 子类必须实现此方法提供默认的脱敏逻辑
     */
    @Override
    public abstract String desensitize(String value);
} 