package io.github.kk01001.localmessage.dao;

import io.github.kk01001.localmessage.entity.LocalMessage;
import io.github.kk01001.localmessage.enums.MessageStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息数据访问接口
 * 用户需要实现此接口来提供数据库操作
 *
 * @author kk01001
 */
public interface LocalMessageDao {
    
    /**
     * 插入消息
     *
     * @param message 消息实体
     * @return 插入成功的记录数
     */
    int insert(LocalMessage message);
    
    /**
     * 根据ID查询消息
     *
     * @param id 消息ID
     * @return 消息实体
     */
    LocalMessage selectById(Long id);
    
    /**
     * 更新消息状态
     *
     * @param id 消息ID
     * @param status 新状态
     * @param errorMessage 错误信息
     * @param version 版本号（乐观锁）
     * @return 更新成功的记录数
     */
    int updateStatus(Long id, MessageStatus status, String errorMessage, Integer version);
    
    /**
     * 更新重试信息
     *
     * @param id 消息ID
     * @param retryCount 重试次数
     * @param nextRetryTime 下次重试时间
     * @param errorMessage 错误信息
     * @param version 版本号（乐观锁）
     * @return 更新成功的记录数
     */
    int updateRetryInfo(Long id, Integer retryCount, LocalDateTime nextRetryTime, String errorMessage, Integer version);
    
    /**
     * 查询待处理的消息
     *
     * @param limit 限制数量
     * @return 待处理的消息列表
     */
    List<LocalMessage> selectPendingMessages(int limit);
    
    /**
     * 查询需要重试的消息
     *
     * @param currentTime 当前时间
     * @param limit 限制数量
     * @return 需要重试的消息列表
     */
    List<LocalMessage> selectRetryMessages(LocalDateTime currentTime, int limit);
    
    /**
     * 批量更新消息状态为处理中
     *
     * @param ids 消息ID列表
     * @return 更新成功的记录数
     */
    int batchUpdateToProcessing(List<Long> ids);
}
