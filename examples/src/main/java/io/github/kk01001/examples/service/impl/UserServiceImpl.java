package io.github.kk01001.examples.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.kk01001.examples.entity.User;
import io.github.kk01001.examples.mapper.UserMapper;
import io.github.kk01001.examples.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public List<User> findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User findByUsernameOne(String username) {
        return userMapper.findByUsernameOne(username);
    }
}