package io.github.archer099.chat.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author archer099
 * @date 2026-03-07 18:00:00
 * @description 好友关系
 */
@Data
@TableName("chat_friendship")
public class ChatFriendship {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long friendId;

    /**
     * 0-申请中 1-已通过 2-已拒绝
     */
    private Integer status;

    /**
     * 验证消息
     */
    private String requestMsg;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
