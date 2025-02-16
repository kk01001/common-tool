package io.github.kk01001.examples.service;

import io.github.kk01001.examples.model.UserInfo;

import java.util.List;

public interface IUserInfoMybatisService {

    int insert(UserInfo userInfo);

    int update(UserInfo userInfo);

    int batchInsert(List<UserInfo> userInfoList);

    int batchUpdate(List<UserInfo> userInfoList);
    
    List<UserInfo> selectByCondition(UserInfo userInfo);

    List<UserInfo> selectByConditionWithParam(UserInfo userInfo);

    List<UserInfo> selectByFields(String name, String phone, String email);
    
    UserInfo selectById(Long id);
}