package io.github.kk01001.localmessage.example;

import io.github.kk01001.localmessage.dao.LocalMessageDao;
import io.github.kk01001.localmessage.entity.LocalMessage;
import io.github.kk01001.localmessage.enums.MessageStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 本地消息DAO实现示例（内存版本，仅用于演示）
 * 实际使用时需要实现真正的数据库操作
 *
 * @author kk01001
 */
@Slf4j
@Repository
public class LocalMessageDaoImpl implements LocalMessageDao {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, LocalMessage> messageStore = new ConcurrentHashMap<>();

    @Override
    public int insert(LocalMessage message) {
        message.setId(idGenerator.getAndIncrement());
        message.setCreateTime(System.currentTimeMillis());
        message.setUpdateTime(System.currentTimeMillis());
        messageStore.put(message.getId(), cloneMessage(message));
        log.info("插入消息: id={}, businessType={}, businessId={}",
                message.getId(), message.getBusinessType(), message.getBusinessId());
        return 1;
    }

    @Override
    public LocalMessage selectById(Long id) {
        LocalMessage message = messageStore.get(id);
        return message != null ? cloneMessage(message) : null;
    }

    @Override
    public int updateStatus(Long id, MessageStatus status, String errorMessage, Integer version) {
        LocalMessage message = messageStore.get(id);
        if (message == null || !message.getVersion().equals(version)) {
            return 0;
        }

        message.setStatus(status);
        message.setErrorMessage(errorMessage);
        message.setUpdateTime(System.currentTimeMillis());
        if (status == MessageStatus.SUCCESS) {
            message.setProcessTime(System.currentTimeMillis());
        }
        message.setVersion(version + 1);

        log.info("更新消息状态: id={}, status={}, version={}", id, status, version + 1);
        return 1;
    }

    @Override
    public int updateRetryInfo(Long id, Integer retryCount, LocalDateTime nextRetryTime,
                               String errorMessage, Integer version) {
        LocalMessage message = messageStore.get(id);
        if (message == null || !message.getVersion().equals(version)) {
            return 0;
        }

        message.setRetryCount(retryCount);
        message.setNextRetryTime(nextRetryTime);
        message.setErrorMessage(errorMessage);
        message.setStatus(MessageStatus.FAILED);
        message.setUpdateTime(System.currentTimeMillis());
        message.setVersion(version + 1);

        log.info("更新重试信息: id={}, retryCount={}, nextRetryTime={}, version={}",
                id, retryCount, nextRetryTime, version + 1);
        return 1;
    }

    @Override
    public List<LocalMessage> selectPendingMessages(int limit) {
        return messageStore.values().stream()
                .filter(msg -> msg.getStatus() == MessageStatus.PENDING)
                .sorted((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()))
                .limit(limit)
                .map(this::cloneMessage)
                .collect(Collectors.toList());
    }

    @Override
    public List<LocalMessage> selectRetryMessages(LocalDateTime currentTime, int limit) {
        return messageStore.values().stream()
                .filter(msg -> msg.getStatus() == MessageStatus.FAILED
                        && msg.getNextRetryTime() != null
                        && msg.getNextRetryTime().isBefore(currentTime)
                        && msg.getRetryCount() < msg.getMaxRetryCount())
                .sorted((a, b) -> a.getNextRetryTime().compareTo(b.getNextRetryTime()))
                .limit(limit)
                .map(this::cloneMessage)
                .collect(Collectors.toList());
    }

    @Override
    public int batchUpdateToProcessing(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            LocalMessage message = messageStore.get(id);
            if (message != null && (message.getStatus() == MessageStatus.PENDING || message.getStatus() == MessageStatus.FAILED)) {
                message.setStatus(MessageStatus.PROCESSING);
                message.setUpdateTime(System.currentTimeMillis());
                count++;
            }
        }
        log.info("批量更新消息状态为处理中: count={}", count);
        return count;
    }

    /**
     * 克隆消息对象，避免外部修改
     */
    private LocalMessage cloneMessage(LocalMessage original) {
        LocalMessage clone = new LocalMessage();
        clone.setId(original.getId());
        clone.setBusinessType(original.getBusinessType());
        clone.setBusinessId(original.getBusinessId());
        clone.setMessageContent(original.getMessageContent());
        clone.setStatus(original.getStatus());
        clone.setRetryCount(original.getRetryCount());
        clone.setMaxRetryCount(original.getMaxRetryCount());
        clone.setNextRetryTime(original.getNextRetryTime());
        clone.setErrorMessage(original.getErrorMessage());
        clone.setCreateTime(original.getCreateTime());
        clone.setUpdateTime(original.getUpdateTime());
        clone.setProcessTime(original.getProcessTime());
        clone.setExtData(original.getExtData());
        clone.setVersion(original.getVersion());
        return clone;
    }
}
