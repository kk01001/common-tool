package io.github.kk01001.example.mybatis.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.kk01001.example.mybatis.entity.User;
import io.github.kk01001.example.mybatis.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 *
 * @author kk01001
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Map<String, Object> getUserPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> page = userService.getUserPage(current, size);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", page.getRecords());
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        return result;
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", user != null);
        result.put("data", user);
        return result;
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Map<String, Object> createUser(@RequestBody User user) {
        boolean success = userService.createUser(user);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "创建成功" : "创建失败");
        return result;
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        boolean success = userService.updateUser(user);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "更新成功" : "更新失败");
        return result;
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    public Map<String, Object> searchUsers(@RequestParam String keyword) {
        List<User> users = userService.searchUsers(keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", users);
        return result;
    }

    /**
     * 根据部门查询用户
     */
    @GetMapping("/department/{department}")
    public Map<String, Object> getUsersByDepartment(@PathVariable String department) {
        List<User> users = userService.getUsersByDepartment(department);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", users);
        return result;
    }
}
