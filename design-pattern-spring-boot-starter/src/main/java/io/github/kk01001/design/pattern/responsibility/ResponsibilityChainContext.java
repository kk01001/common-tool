package io.github.kk01001.design.pattern.responsibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <T> 处理的数据类型
 * @param <R> 返回的结果类型
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 责任链模式中用于在处理器之间传递和共享数据的上下文对象
 */
public class ResponsibilityChainContext<T, R> {
    /**
     * 上下文数据，用于在责任链各处理器之间共享数据
     */
    private final Map<String, Object> contextData = new HashMap<>();

    /**
     * 原始数据
     */
    private T data;

    /**
     * 是否终止责任链的执行标志
     */
    private boolean terminated = false;

    /**
     * 处理器执行历史记录
     */
    private final List<String> handlerHistory = new ArrayList<>();

    /**
     * 处理结果收集
     */
    private final List<R> resultCollection = new ArrayList<>();

    /**
     * 设置上下文数据
     *
     * @param key   键
     * @param value 值
     * @return 当前上下文实例
     */
    public ResponsibilityChainContext<T, R> set(String key, Object value) {
        contextData.put(key, value);
        return this;
    }

    /**
     * 获取上下文数据
     *
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
     *
     * @param key          键
     * @param defaultValue 默认值
     * @param <V>          值类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <V> V getOrDefault(String key, V defaultValue) {
        Object value = contextData.get(key);
        return value == null ? defaultValue : (V) value;
    }

    /**
     * 检查上下文是否包含指定键
     *
     * @param key 键
     * @return 是否包含
     */
    public boolean contains(String key) {
        return contextData.containsKey(key);
    }

    /**
     * 移除上下文数据
     *
     * @param key 键
     * @return 当前上下文实例
     */
    public ResponsibilityChainContext<T, R> remove(String key) {
        contextData.remove(key);
        return this;
    }

    /**
     * 清空上下文数据
     *
     * @return 当前上下文实例
     */
    public ResponsibilityChainContext<T, R> clear() {
        contextData.clear();
        return this;
    }

    /**
     * 终止责任链的执行
     *
     * @return 当前上下文实例
     */
    public ResponsibilityChainContext<T, R> terminate() {
        this.terminated = true;
        return this;
    }

    /**
     * 重置终止状态
     *
     * @return 当前上下文实例
     */
    public ResponsibilityChainContext<T, R> resetTermination() {
        this.terminated = false;
        return this;
    }

    /**
     * 检查责任链是否已被终止
     *
     * @return 是否已终止
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * 记录处理器执行历史
     *
     * @param handlerName 处理器名称
     * @return 当前上下文实例
     */
    public ResponsibilityChainContext<T, R> recordHandler(String handlerName) {
        handlerHistory.add(handlerName);
        return this;
    }

    /**
     * 获取处理器执行历史
     *
     * @return 处理器执行历史列表
     */
    public List<String> getHandlerHistory() {
        return new ArrayList<>(handlerHistory);
    }

    /**
     * 收集处理结果
     *
     * @param result 处理结果
     * @return 当前上下文实例
     */
    public ResponsibilityChainContext<T, R> collectResult(R result) {
        if (result != null) {
            resultCollection.add(result);
        }
        return this;
    }

    /**
     * 获取所有处理结果
     *
     * @return 处理结果列表
     */
    public List<R> getResults() {
        return new ArrayList<>(resultCollection);
    }

    /**
     * 获取原始数据
     *
     * @return 原始数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置原始数据
     *
     * @param data 原始数据
     * @return 当前上下文实例
     */
    public ResponsibilityChainContext<T, R> setData(T data) {
        this.data = data;
        return this;
    }
}