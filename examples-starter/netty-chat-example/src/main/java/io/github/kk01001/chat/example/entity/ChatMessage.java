package io.github.kk01001.chat.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kk01001
 * @date 2026-03-07 18:00:00
 * @description 聊天消息
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long senderId;

    /**
     * 接收者ID（单聊）
     */
    private Long receiverId;

    /**
     * 群组ID（群聊）
     */
    private Long groupId;

    /**
     * PRIVATE-单聊 GROUP-群聊
     */
    private String chatType;

    /**
     * TEXT/IMAGE/FILE/AUDIO/SYSTEM
     */
    private String msgType;

    private String content;

    private Long fileId;

    /**
     * 0-未读 1-已读 2-已撤回
     */
    private Integer status;

    private LocalDateTime createTime;
}
