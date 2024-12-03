package io.github.kk01001.transation;

import org.springframework.transaction.support.TransactionSynchronization;

/**
 * @author linshiqiang
 * date 2022-11-10 21:55:00
 */
public class DoTransactionCompletion implements TransactionSynchronization {

    private final Runnable runnable;

    public DoTransactionCompletion(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void afterCompletion(int status) {
        // 再事务提交之后, 异步做一些事情
        if (status == TransactionSynchronization.STATUS_COMMITTED) {
            this.runnable.run();
        }
    }
}
