package io.github.kk01001.examples.controller;

import io.github.kk01001.examples.model.UserInfo;
import io.github.kk01001.examples.service.IUserInfoMybatisPlusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mybatis-plus/user-info")
@RequiredArgsConstructor
public class UserInfoMybatisPlusController {

    private final IUserInfoMybatisPlusService userInfoService;

    @PostMapping
    public boolean insert(@RequestBody UserInfo userInfo) {
        return userInfoService.insert(userInfo);
    }

    @PutMapping
    public boolean update(@RequestBody UserInfo userInfo) {
        return userInfoService.update(userInfo);
    }

    @PostMapping("/batchDelete")
    public int batchDelete(@RequestBody List<String> userInfoList) {
        return userInfoService.batchDelete(userInfoList);
    }

    @PostMapping("/batch")
    public boolean batchInsert(@RequestBody List<UserInfo> userInfoList) {
        return userInfoService.batchInsert(userInfoList);
    }

    @PutMapping("/batch")
    public boolean batchUpdate(@RequestBody List<UserInfo> userInfoList) {
        return userInfoService.batchUpdate(userInfoList);
    }

    @GetMapping("/condition")
    public List<UserInfo> selectByCondition(@RequestBody UserInfo userInfo) {
        return userInfoService.selectByCondition(userInfo);
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