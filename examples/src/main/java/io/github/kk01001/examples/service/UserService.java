package io.github.kk01001.examples.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.kk01001.examples.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserService extends IService<User> {

    List<User> findByUsername(@Param("username") String username);

    User findByUsernameOne(@Param("username") String username);
} 