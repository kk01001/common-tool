package io.github.kk01001.examples.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.kk01001.examples.dto.UserExcelDTO;
import io.github.kk01001.examples.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT COUNT(*) FROM t_user")
    long count();

    @Select("""
            SELECT 
                id,
                username,
                email,
                status,
                type
            FROM t_user
            LIMIT #{offset}, #{size}
            """)
    List<UserExcelDTO> queryUserExcelList(@Param("offset") int offset, @Param("size") int size);
} 