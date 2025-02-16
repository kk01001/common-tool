package io.github.kk01001.examples.mapper;

import io.github.kk01001.examples.model.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserInfoMapper {
    // 新增单条记录
    int insert(UserInfo userInfo);
    
    // 更新单条记录
    int update(UserInfo userInfo);
    
    // 批量新增
    int batchInsert(@Param("list") List<UserInfo> userInfoList);
    
    // 批量更新
    int batchUpdate(@Param("list") List<UserInfo> userInfoList);

    /**
     * 根据实体类条件查询 无 @Param
     * @Param("param")
     */
    List<UserInfo> selectByCondition(UserInfo userInfo);

    List<UserInfo> selectByConditionWithParam(@Param("param")UserInfo userInfo);
    
    // 根据多个字段查询
    List<UserInfo> selectByFields(@Param("name") String name, 
                                 @Param("phone") String phone, 
                                 @Param("email") String email);
    
    // 根据ID查询单条记录
    UserInfo selectById(@Param("id") Long id);
}