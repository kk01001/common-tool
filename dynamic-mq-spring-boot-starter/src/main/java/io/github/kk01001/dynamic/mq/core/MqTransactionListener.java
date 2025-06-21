package io.github.kk01001.dynamic.mq.core;

import io.github.kk01001.dynamic.mq.enums.TransactionState;
import io.github.kk01001.dynamic.mq.model.MqMessage;

/**
 * @author linshiqiang
 * @date 2025-06-21 21:41:29
 * @description MQ事务监听器接口，用户需要实现此接口来处理事务逻辑
 */
public interface MqTransactionListener {
    
    /**
     * 执行本地事务
     * 当发送事务消息时，会回调此方法执行本地事务
     *
     * @param message 消息对象
     * @param transactionId 事务ID
     * @return 本地事务执行结果
     */
    TransactionState executeLocalTransaction(MqMessage message, String transactionId);
    
    /**
     * 检查本地事务状态
     * 当MQ服务器需要确认事务状态时，会回调此方法
     *
     * @param message 消息对象
     * @param transactionId 事务ID
     * @return 本地事务状态
     */
    TransactionState checkLocalTransaction(MqMessage message, String transactionId);
}
