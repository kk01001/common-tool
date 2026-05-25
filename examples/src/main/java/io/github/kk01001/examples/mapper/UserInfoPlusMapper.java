package io.github.archer099.examples.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.archer099.examples.model.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoPlusMapper extends BaseMapper<UserInfo> {
}