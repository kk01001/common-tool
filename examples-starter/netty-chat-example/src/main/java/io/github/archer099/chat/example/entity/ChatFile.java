package io.github.archer099.chat.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author archer099
 * @date 2026-03-07 19:00:00
 * @description 聊天文件（数据库只存元信息和 URL，文件存储由策略决定）
 */
@Data
@TableName("chat_file")
public class ChatFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String contentType;

    private Long fileSize;

    /**
     * 文件存储路径/URL
     */
    private String url;

    /**
     * 存储类型: local / oss
     */
    private String storageType;

    private Long uploaderId;

    private LocalDateTime createTime;
}
