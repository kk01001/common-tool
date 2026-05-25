package io.github.archer099.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.archer099.account.entity.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
