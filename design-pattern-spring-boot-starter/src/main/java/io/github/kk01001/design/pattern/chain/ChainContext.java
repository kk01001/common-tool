package io.github.kk01001.design.pattern.chain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 责任链模式中用于在处理器之间传递和共享数据的上下文对象
 * @param <T> 处理的数据类型
 * @param <R> 返回的结果类型
 */
public class ChainContext<T, R> {
    /**
     * 上下文数据，用于在责任链各处理器之间共享数据
     */
    private final Map<String, Object> contextData = new HashMap<>();
    
    /**
     * 设置上下文数据
     * @param key 键
     * @param value 值
     * @return 当前上下文实例
     */
    public ChainContext<T, R> set(String key, Object value) {
        contextData.put(key, value);
        return this;
    }
    
    /**
     * 获取上下文数据
     * @param key 键
     * @param <V> 值类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <V> V get(String key) {
        return (V) contextData.get(key);
    }
    
    /**
     * 获取上下文数据，如果不存在则返回默认值
     * @param key 键
     * @param defaultValue 默认值
     * @param <V> 值类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <V> V getOrDefault(String key, V defaultValue) {
        Object value = contextData.get(key);
        return value == null ? defaultValue : (V) value;
    }
    
    /**
     * 检查上下文是否包含指定键
     * @param key 键
     * @return 是否包含
     */
    public boolean contains(String key) {
        return contextData.containsKey(key);
    }
    
    /**
     * 移除上下文数据
     * @param key 键
     * @return 当前上下文实例
     */
    public ChainContext<T, R> remove(String key) {
        contextData.remove(key);
        return this;
    }
    
    /**
     * 清空上下文数据
     * @return 当前上下文实例
     */
    public ChainContext<T, R> clear() {
        contextData.clear();
        return this;
    }
}