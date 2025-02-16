package io.github.kk01001.examples.service.impl;

import io.github.kk01001.examples.mapper.UserInfoMapper;
import io.github.kk01001.examples.model.UserInfo;
import io.github.kk01001.examples.service.IUserInfoMybatisService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoMybatisServiceImpl implements IUserInfoMybatisService {
    
    @Resource
    private UserInfoMapper userInfoMapper;
    
    @Override
    public int insert(UserInfo userInfo) {
        return userInfoMapper.insert(userInfo);
    }
    
    @Override
    public int update(UserInfo userInfo) {
        return userInfoMapper.update(userInfo);
    }
    
    @Override
    public int batchInsert(List<UserInfo> userInfoList) {
        return userInfoMapper.batchInsert(userInfoList);
    }
    
    @Override
    public int batchUpdate(List<UserInfo> userInfoList) {
        return userInfoMapper.batchUpdate(userInfoList);
    }
    
    @Override
    public List<UserInfo> selectByCondition(UserInfo userInfo) {
        return userInfoMapper.selectByCondition(userInfo);
    }

    @Override
    public List<UserInfo> selectByConditionWithParam(UserInfo userInfo) {
        return userInfoMapper.selectByConditionWithParam(userInfo);
    }

    @Override
    public List<UserInfo> selectByFields(String name, String phone, String email) {
        return userInfoMapper.selectByFields(name, phone, email);
    }
    
    @Override
    public UserInfo selectById(Long id) {
        return userInfoMapper.selectById(id);
    }
}