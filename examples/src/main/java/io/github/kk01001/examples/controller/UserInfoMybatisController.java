package io.github.kk01001.examples.controller;

import io.github.kk01001.examples.model.UserInfo;
import io.github.kk01001.examples.service.IUserInfoMybatisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mybatis/user-info")
@RequiredArgsConstructor
public class UserInfoMybatisController {

    private final IUserInfoMybatisService userInfoService;

    @PostMapping
    public int insert(@RequestBody UserInfo userInfo) {
        return userInfoService.insert(userInfo);
    }

    @PutMapping
    public int update(@RequestBody UserInfo userInfo) {
        return userInfoService.update(userInfo);
    }

    @PostMapping("/batch")
    public int batchInsert(@RequestBody List<UserInfo> userInfoList) {
        return userInfoService.batchInsert(userInfoList);
    }

    @PutMapping("/batch")
    public int batchUpdate(@RequestBody List<UserInfo> userInfoList) {
        return userInfoService.batchUpdate(userInfoList);
    }

    @GetMapping("/condition")
    public List<UserInfo> selectByCondition(@RequestBody UserInfo userInfo) {
        return userInfoService.selectByCondition(userInfo);
    }

    @GetMapping("/conditionWithParam")
    public List<UserInfo> selectByConditionWithParam(@RequestBody UserInfo userInfo) {
        return userInfoService.selectByConditionWithParam(userInfo);
    }

    @GetMapping("/fields")
    public List<UserInfo> selectByFields(@RequestParam(required = false) String name,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) String email) {
        return userInfoService.selectByFields(name, phone, email);
    }

    @GetMapping("/{id}")
    public UserInfo selectById(@PathVariable Long id) {
        return userInfoService.selectById(id);
    }
}