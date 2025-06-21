package io.github.kk01001.dynamic.mq.enums;

/**
 * @author linshiqiang
 * @date 2025-06-21 21:41:29
 * @description 事务状态枚举
 */
public enum TransactionState {
    
    /**
     * 提交事务，消息将被投递给消费者
     */
    COMMIT,
    
    /**
     * 回滚事务，消息将被丢弃
     */
    ROLLBACK,
    
    /**
     * 未知状态，等待下次检查
     */
    UNKNOWN
}
