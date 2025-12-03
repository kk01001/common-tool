package io.github.kk01001.account.controller;

import io.github.kk01001.account.service.AccountService;
import io.github.kk01001.seata.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/debit")
    public Result<Void> debit(@RequestParam String userId, @RequestParam Integer money) {
        accountService.debit(userId, money);
        return Result.success();
    }
}
