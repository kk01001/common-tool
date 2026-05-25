package io.github.archer099.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.archer099.account.entity.Account;
import io.github.archer099.account.mapper.AccountMapper;
import io.github.archer099.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void debit(String userId, int money) {
        log.info("Account Service Begin ... xid: {}", io.seata.core.context.RootContext.getXID());
        Account account = accountMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, userId));
        if (account == null) {
            throw new RuntimeException("账户不存在");
        }
        if (account.getMoney() < money) {
            throw new RuntimeException("余额不足");
        }
        account.setMoney(account.getMoney() - money);
        accountMapper.updateById(account);
        log.info("Account Service End ... ");
    }
}
