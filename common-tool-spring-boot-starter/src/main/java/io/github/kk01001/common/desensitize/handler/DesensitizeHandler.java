package io.github.kk01001.common.desensitize.handler;

public interface DesensitizeHandler {
    
    /**
     * 脱敏处理
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