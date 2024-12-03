package io.github.kk01001.transation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author linshiqiang
 * date 2022-11-10 21:59:00
 */
@Slf4j
public class TransactionUtils {

    public static void doAfterTransaction(DoTransactionCompletion doTransactionCompletion) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(doTransactionCompletion);
        }
    }

    public static void doAfterTransaction(Runnable runnable) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new DoTransactionCompletion(runnable));
            return;
        }

        // 无事务
        runnable.run();
    }

}
