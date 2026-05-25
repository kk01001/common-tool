package io.github.archer099.localmessage.service;

import io.github.archer099.localmessage.dao.LocalMessageDao;
import io.github.archer099.localmessage.entity.LocalMessage;
import io.github.archer099.localmessage.enums.MessageStatus;
import io.github.archer099.localmessage.processor.MessageProcessor;
import io.github.archer099.localmessage.processor.MessageProcessorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息服务
 *
 * @author archer099
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalMessageService {

    private final LocalMessageDao localMessageDao;
    private final MessageProcessorRegistry processorRegistry;

    /**
     * 发送本地消息
     * 在业务事务中调用，保证消息和业务数据的一致性
     *
     * @param businessType   业务类型
     * @param businessId     业务ID
     * @param messageContent 消息内容
     * @param maxRetryCount  最大重试次数
     * @param extData        扩展数据
     * @return 消息ID
     */
    // @Transactional(rollbackFor = Exception.class) // 用户需要在使用时添加事务注解
    public Long sendMessage(String businessType, String businessId, String messageContent,
                            Integer maxRetryCount, String extData) {
        // 验证业务类型是否有对应的处理器
        if (!processorRegistry.hasProcessor(businessType)) {
            throw new IllegalArgumentException("未找到业务类型对应的处理器: " + businessType);
        }

        LocalMessage message = new LocalMessage();
        message.setBusinessType(businessType);
        message.setBusinessId(businessId);
        message.setMessageContent(messageContent);
        message.setStatus(MessageStatus.PENDING);
        message.setRetryCount(0);
        message.setMaxRetryCount(maxRetryCount != null ? maxRetryCount : 3);
        message.setExtData(extData);
        message.setCreateTime(System.currentTimeMillis());
        message.setUpdateTime(System.currentTimeMillis());
        message.setVersion(0);

        localMessageDao.insert(message);

        log.info("发送本地消息成功: businessType={}, businessId={}, messageId={}",
                businessType, businessId, message.getId());

        return message.getId();
    }

    /**
     * 处理单个消息
     *
     * @param message 消息实体
     */
    public void processMessage(LocalMessage message) {
        try {
            MessageProcessor processor = processorRegistry.getProcessor(message.getBusinessType());
            MessageProcessor.ProcessResult result = processor.process(message);

            if (result.isSuccess()) {
                // 处理成功
                updateMessageStatus(message.getId(), MessageStatus.SUCCESS, null, message.getVersion());
                log.info("消息处理成功: messageId={}, businessType={}",
                        message.getId(), message.getBusinessType());
                return;
            }
            // 处理失败
            handleProcessFailure(message, result);
        } catch (Exception e) {
            log.error("消息处理异常: messageId={}, businessType={}",
                    message.getId(), message.getBusinessType(), e);
            handleProcessException(message, e);
        }
    }

    /**
     * 处理失败情况
     */
    private void handleProcessFailure(LocalMessage message, MessageProcessor.ProcessResult result) {
        if (result.isNeedRetry() && message.getRetryCount() < message.getMaxRetryCount()) {
            // 需要重试且未达到最大重试次数
            int newRetryCount = message.getRetryCount() + 1;
            LocalDateTime nextRetryTime = calculateNextRetryTime(newRetryCount, result.getRetryDelaySeconds());

            updateRetryInfo(message.getId(), newRetryCount, nextRetryTime,
                    result.getErrorMessage(), message.getVersion());

            log.warn("消息处理失败，将重试: messageId={}, retryCount={}, nextRetryTime={}, error={}",
                    message.getId(), newRetryCount, nextRetryTime, result.getErrorMessage());
            return;
        }
        // 不需要重试或达到最大重试次数
        MessageStatus finalStatus = message.getRetryCount() >= message.getMaxRetryCount()
                ? MessageStatus.MAX_RETRY_REACHED : MessageStatus.FAILED;

        updateMessageStatus(message.getId(), finalStatus, result.getErrorMessage(), message.getVersion());

        log.error("消息处理最终失败: messageId={}, status={}, error={}",
                message.getId(), finalStatus, result.getErrorMessage());
    }

    /**
     * 处理异常情况
     */
    private void handleProcessException(LocalMessage message, Exception e) {
        if (message.getRetryCount() < message.getMaxRetryCount()) {
            // 未达到最大重试次数，安排重试
            int newRetryCount = message.getRetryCount() + 1;
            LocalDateTime nextRetryTime = calculateNextRetryTime(newRetryCount, null);

            updateRetryInfo(message.getId(), newRetryCount, nextRetryTime,
                    e.getMessage(), message.getVersion());
            return;
        }
        // 达到最大重试次数
        updateMessageStatus(message.getId(), MessageStatus.MAX_RETRY_REACHED,
                e.getMessage(), message.getVersion());
    }

    /**
     * 计算下次重试时间
     */
    private LocalDateTime calculateNextRetryTime(int retryCount, Long customDelaySeconds) {
        long delaySeconds;
        if (customDelaySeconds != null) {
            delaySeconds = customDelaySeconds;
        } else {
            // 指数退避策略：1分钟、2分钟、4分钟、8分钟...
            delaySeconds = (long) Math.pow(2, retryCount - 1) * 60;
            // 最大延迟30分钟
            delaySeconds = Math.min(delaySeconds, 30 * 60);
        }
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

    /**
     * 更新消息状态
     */
    private void updateMessageStatus(Long messageId, MessageStatus status, String errorMessage, Integer version) {
        int updated = localMessageDao.updateStatus(messageId, status, errorMessage, version);
        if (updated == 0) {
            log.warn("更新消息状态失败，可能存在并发冲突: messageId={}", messageId);
        }
    }

    /**
     * 更新重试信息
     */
    private void updateRetryInfo(Long messageId, Integer retryCount, LocalDateTime nextRetryTime,
                                 String errorMessage, Integer version) {
        int updated = localMessageDao.updateRetryInfo(messageId, retryCount, nextRetryTime, errorMessage, version);
        if (updated == 0) {
            log.warn("更新重试信息失败，可能存在并发冲突: messageId={}", messageId);
        }
    }

    /**
     * 获取待处理的消息
     */
    public List<LocalMessage> getPendingMessages(int limit) {
        return localMessageDao.selectPendingMessages(limit);
    }

    /**
     * 获取需要重试的消息
     */
    public List<LocalMessage> getRetryMessages(int limit) {
        return localMessageDao.selectRetryMessages(LocalDateTime.now(), limit);
    }

    /**
     * 批量更新消息状态为处理中
     */
    public void batchUpdateToProcessing(List<Long> messageIds) {
        if (!messageIds.isEmpty()) {
            localMessageDao.batchUpdateToProcessing(messageIds);
        }
    }
}
