package io.github.kk01001.examples.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.kk01001.examples.dto.UserExcelDTO;
import io.github.kk01001.examples.dto.UserQueryDTO;
import io.github.kk01001.examples.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 统计用户总数
     */
    long count();

    /**
     * 分页查询用户导出数据
     * @param offset 偏移量
     * @param size 每页大小
     * @return 用户列表
     */
    List<UserExcelDTO> queryUserExcelList(@Param("offset") int offset, @Param("size") int size);

    /**
     * 批量插入用户
     * @param users 用户列表
     */
    void insertBatch(@Param("list") List<User> users);

    @Select("SELECT * FROM t_user WHERE username = #{username}")
    List<User> findByUsername(@Param("username") String username);

    @Select("SELECT * FROM t_user WHERE username = #{name} limit 1")
    User findByUsernameOne(@Param("name") String username);

    User queryUser(@Param("query") UserQueryDTO queryDTO);
} 