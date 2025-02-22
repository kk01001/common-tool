package io.github.kk01001.oauth2.authentication.social;

import lombok.Data;

/**
 * @author kk01001
 * @date 2025-02-22 14:31:00
 * @description 社交用户信息
 */
@Data
public class SocialUserInfo {

    /**
     * 社交平台ID
     */
    private String socialId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别
     */
    private String gender;

    /**
     * 其他信息
     */
    private Object additionalInfo;
} 