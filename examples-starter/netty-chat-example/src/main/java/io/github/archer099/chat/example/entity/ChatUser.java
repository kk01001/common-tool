package io.github.archer099.chat.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author archer099
 * @date 2026-03-07 18:00:00
 * @description 聊天用户
 */
@Data
@TableName("chat_user")
public class ChatUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    /**
     * 头像文件ID
     */
    private Long avatarFileId;

    /**
     * 1-正常 0-禁用
     */
    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
