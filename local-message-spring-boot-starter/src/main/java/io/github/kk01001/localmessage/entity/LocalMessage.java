package io.github.kk01001.localmessage.entity;

import io.github.kk01001.localmessage.enums.MessageStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 本地消息表实体
 * 用户需要在数据库中创建对应的表结构
 *
 * @author kk01001
 */
@Data
public class LocalMessage {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 业务类型，用于区分不同的消息处理器
     */
    private String businessType;

    /**
     * 业务ID，关联的业务数据主键
     */
    private String businessId;

    /**
     * 消息内容，JSON格式存储
     */
    private String messageContent;

    /**
     * 消息状态
     */
    private MessageStatus status;

    /**
     * 当前重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 处理时间
     */
    private Long processTime;

    /**
     * 扩展字段，JSON格式
     */
    private String extData;

    /**
     * 版本号，用于乐观锁
     */
    private Integer version;
}
