package io.github.kk01001.examples.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.kk01001.examples.entity.User;
import io.github.kk01001.examples.mapper.UserMapper;
import io.github.kk01001.examples.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
} 