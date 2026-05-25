package io.github.archer099.chat.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author archer099
 * @date 2026-03-07 18:00:00
 * @description 会话
 */
@Data
@TableName("chat_conversation")
public class ChatConversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 对方用户ID或群组ID
     */
    private Long targetId;

    /**
     * PRIVATE-单聊 GROUP-群聊
     */
    private String chatType;

    private Long lastMsgId;

    private String lastMsgContent;

    private LocalDateTime lastMsgTime;

    private Integer unreadCount;

    /**
     * 1-正常 0-已删除
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
