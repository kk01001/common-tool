package io.github.archer099.example.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.archer099.example.mybatis.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 *
 * @author archer099
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
