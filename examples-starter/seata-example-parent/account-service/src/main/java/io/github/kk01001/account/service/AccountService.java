package io.github.archer099.account.service;

public interface AccountService {
    /**
     * 扣减余额
     *
     * @param userId 用户ID
     * @param money  扣减金额
     */
    void debit(String userId, int money);
}
