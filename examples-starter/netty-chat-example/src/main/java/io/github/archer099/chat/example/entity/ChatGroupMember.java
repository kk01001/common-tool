package io.github.archer099.chat.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author archer099
 * @date 2026-03-07 18:00:00
 * @description 群成员
 */
@Data
@TableName("chat_group_member")
public class ChatGroupMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long userId;

    /**
     * 0-成员 1-管理员 2-群主
     */
    private Integer role;

    /**
     * 群内昵称
     */
    private String nickname;

    private LocalDateTime joinTime;
}
