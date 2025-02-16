package io.github.kk01001.examples.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.kk01001.examples.model.UserInfo;

import java.util.List;

public interface IUserInfoMybatisPlusService extends IService<UserInfo> {
    
    boolean insert(UserInfo userInfo);
    
    boolean update(UserInfo userInfo);

    int batchDelete(List<String> userInfoList);

    boolean batchInsert(List<UserInfo> userInfoList);
    
    boolean batchUpdate(List<UserInfo> userInfoList);
    
    List<UserInfo> selectByCondition(UserInfo userInfo);
    
    List<UserInfo> selectByFields(String name, String phone, String email);
    
    UserInfo selectById(Long id);

}