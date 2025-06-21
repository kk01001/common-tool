package io.github.kk01001.dynamic.mq.manager;

import io.github.kk01001.dynamic.mq.core.MqTransactionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2025-06-21 21:41:29
 * @description MQ事务管理器，管理用户注册的事务监听器
 */
@Slf4j
@Component
public class MqTransactionManager {
    
    /**
     * 事务监听器注册表
     * Key: 事务类型或业务标识
     * Value: 事务监听器
     */
    private final Map<String, MqTransactionListener> transactionListeners = new ConcurrentHashMap<>();
    
    /**
     * 默认事务监听器
     */
    private MqTransactionListener defaultTransactionListener;
    
    /**
     * 注册事务监听器
     *
     * @param transactionType 事务类型
     * @param listener 事务监听器
     */
    public void registerTransactionListener(String transactionType, MqTransactionListener listener) {
        transactionListeners.put(transactionType, listener);
        log.info("注册事务监听器: transactionType={}, listener={}", transactionType, listener.getClass().getSimpleName());
    }
    
    /**
     * 设置默认事务监听器
     *
     * @param listener 默认事务监听器
     */
    public void setDefaultTransactionListener(MqTransactionListener listener) {
        this.defaultTransactionListener = listener;
        log.info("设置默认事务监听器: {}", listener.getClass().getSimpleName());
    }
    
    /**
     * 获取事务监听器
     *
     * @param transactionType 事务类型
     * @return 事务监听器
     */
    public MqTransactionListener getTransactionListener(String transactionType) {
        MqTransactionListener listener = transactionListeners.get(transactionType);
        if (listener != null) {
            return listener;
        }
        
        if (defaultTransactionListener != null) {
            return defaultTransactionListener;
        }
        
        throw new IllegalStateException("未找到事务监听器: transactionType=" + transactionType);
    }
    
    /**
     * 移除事务监听器
     *
     * @param transactionType 事务类型
     */
    public void removeTransactionListener(String transactionType) {
        MqTransactionListener removed = transactionListeners.remove(transactionType);
        if (removed != null) {
            log.info("移除事务监听器: transactionType={}", transactionType);
        }
    }
    
    /**
     * 检查是否有事务监听器
     *
     * @param transactionType 事务类型
     * @return 是否存在
     */
    public boolean hasTransactionListener(String transactionType) {
        return transactionListeners.containsKey(transactionType) || defaultTransactionListener != null;
    }
    
    /**
     * 获取所有注册的事务类型
     *
     * @return 事务类型集合
     */
    public java.util.Set<String> getAllTransactionTypes() {
        return transactionListeners.keySet();
    }
    
    /**
     * 清空所有事务监听器
     */
    public void clear() {
        transactionListeners.clear();
        defaultTransactionListener = null;
        log.info("清空所有事务监听器");
    }
}
