package io.github.kk01001.chat.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kk01001
 * @date 2026-03-07 18:00:00
 * @description 群组
 */
@Data
@TableName("chat_group")
public class ChatGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long avatarFileId;

    private Long ownerId;

    private String notice;

    private Integer maxMembers;

    /**
     * 1-正常 0-已解散
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
