package io.github.kk01001.examples.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.kk01001.examples.dto.UserQueryDTO;
import io.github.kk01001.examples.entity.User;
import io.github.kk01001.examples.mapper.UserMapper;
import io.github.kk01001.examples.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;
    
    private static final String[] STATUS_ARRAY = {"0", "1"};
    private static final String[] TYPE_ARRAY = {"normal", "vip"};
    
    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public Page<User> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username) {
        
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
                .like(username != null, User::getUsername, username)
                .orderByDesc(User::getId);
        
        return userService.page(new Page<>(pageNum, pageSize), wrapper);
    }
    
    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 根据username查询2
     */
    @GetMapping("/username/{username}")
    public User getByUsername(@PathVariable String username) {
        return userService.getOne(Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username)
                .eq(User::getType, 1));
    }

    @GetMapping("/findByUsernameOne/{username}")
    public User findByUsernameOne(@PathVariable String username) {
        return userService.findByUsernameOne(username);
    }

    @GetMapping("/findByUsername/{username}")
    public List<User> findByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @GetMapping("/findByUsernameDTO")
    public User findByUsernameDTO(UserQueryDTO userQueryDTO) {
        return userMapper.queryUser(userQueryDTO);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public User save(@RequestBody User user) {
        userService.save(user);
        return user;
    }

    
    /**
     * 批量生成测试数据
     */
    @PostMapping("/batch")
    public boolean batchSave(@RequestParam(defaultValue = "10") Integer count) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < 100; i++) {
            executor.execute(() -> {
                List<User> users = new ArrayList<>();
                Random random = new Random();
                for (int j = 0; j < count; j++) {
                    User user = new User();
                    user.setUsername("test" + j);
                    user.setStatus(STATUS_ARRAY[random.nextInt(STATUS_ARRAY.length)]);
                    user.setType(TYPE_ARRAY[random.nextInt(TYPE_ARRAY.length)]);
                    user.setEmail("test" + j + "@example.com");
                    users.add(user);
                }
                userService.saveBatch(users);
            });
        }

        return true;
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return userService.removeById(id);
    }
} 