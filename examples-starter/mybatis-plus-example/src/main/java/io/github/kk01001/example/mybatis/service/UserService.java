package io.github.kk01001.example.mybatis.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.kk01001.example.mybatis.entity.User;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author kk01001
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean createUser(User user);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);

    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUser(User user);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);

    /**
     * 分页查询用户
     *
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    Page<User> getUserPage(int current, int size);

    /**
     * 根据部门查询用户
     *
     * @param department 部门名称
     * @return 用户列表
     */
    List<User> getUsersByDepartment(String department);

    /**
     * 搜索用户
     *
     * @param keyword 关键词
     * @return 用户列表
     */
    List<User> searchUsers(String keyword);
}
