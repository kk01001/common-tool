/*
 * @Author: linshiqiang
 * @Date: 2025-02-06 15:22:29
 * @Description: Do not edit
 */
package io.github.kk01001.desensitize.handler;

import io.github.kk01001.desensitize.annotation.Desensitize;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
public interface DesensitizeHandler {
    
    /**
     * 带注解参数的脱敏处理
     *
     * @param value 原始值
     * @param annotation 脱敏注解
     * @return 脱敏后的值
     */
    String desensitize(String value, Desensitize annotation);

    /**
     * 使用默认参数的脱敏处理
     *
     * @param value 原始值
     * @return 脱敏后的值
     */
    String desensitize(String value);
    
    /**
     * 反向处理(用于查询条件处理)
     *
     * @param value 脱敏值
     * @return 可能的原始值集合
     */
    default String[] reverse(String value) {
        return new String[]{value};
    }
} 