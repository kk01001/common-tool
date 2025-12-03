package io.github.kk01001.order.feign;

import io.github.kk01001.seata.common.result.Result;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(value = "account-service")
public interface AccountClient {

    /**
     * 扣款
     *
     * @param userId 用户ID
     * @param money  金额
     * @return 结果
     */
    @PostExchange("/account/debit")
    Result<Void> debit(@RequestParam("userId") String userId, @RequestParam("money") Integer money);
}
