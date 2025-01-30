package io.github.kk01001.examples.controller;

import io.github.kk01001.examples.model.UserInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/desensitize")
public class DesensitizeController {

    /**
     * 脱敏测试
     */
    @GetMapping("/test")
    public UserInfo test() {
        UserInfo userInfo = new UserInfo();
        userInfo.setName("张三丰");
        userInfo.setPhone("13812345678");
        userInfo.setEmail("zhangsan@example.com");
        userInfo.setIdCard("330102199901011234");
        userInfo.setBankCard("6222021234567890123");
        userInfo.setAddress("浙江省杭州市西湖区文三路100号");
        userInfo.setPassword("abc123456");
        return userInfo;
    }
} 