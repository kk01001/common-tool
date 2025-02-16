package io.github.kk01001.examples.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.kk01001.examples.mapper.UserInfoPlusMapper;
import io.github.kk01001.examples.model.UserInfo;
import io.github.kk01001.examples.service.IUserInfoMybatisPlusService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoMybatisPlusServiceImpl extends ServiceImpl<UserInfoPlusMapper, UserInfo> implements IUserInfoMybatisPlusService {
    
    @Override
    public boolean insert(UserInfo userInfo) {
        return baseMapper.insert(userInfo) > 0;
    }
    
    @Override
    public boolean update(UserInfo userInfo) {
        return baseMapper.updateById(userInfo) > 0;
    }

    @Override
    public int batchDelete(List<String> userInfoList) {
        return baseMapper.deleteByIds(userInfoList);
    }

    @Override
    public boolean batchInsert(List<UserInfo> userInfoList) {
        return saveBatch(userInfoList);
    }
    
    @Override
    public boolean batchUpdate(List<UserInfo> userInfoList) {
        return updateBatchById(userInfoList);
    }
    
    @Override
    public List<UserInfo> selectByCondition(UserInfo userInfo) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.setEntity(userInfo);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public List<UserInfo> selectByFields(String name, String phone, String email) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(name != null, UserInfo::getName, name)
              .eq(phone != null, UserInfo::getPhone, phone)
              .eq(email != null, UserInfo::getEmail, email);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    public UserInfo selectById(Long id) {
        return baseMapper.selectById(id);
    }
}