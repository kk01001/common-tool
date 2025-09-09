package io.github.kk01001.localmessage.scheduler;

import io.github.kk01001.localmessage.config.LocalMessageProperties;
import io.github.kk01001.localmessage.entity.LocalMessage;
import io.github.kk01001.localmessage.service.LocalMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 消息调度器
 * 定时扫描待处理和需要重试的消息
 *
 * @author kk01001
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "local-message", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MessageScheduler {
    
    private final LocalMessageService localMessageService;
    private final LocalMessageProperties properties;
    private final ThreadPoolExecutor messageProcessExecutor;
    
    /**
     * 处理待处理的消息
     */
    @Scheduled(fixedDelayString = "${local-message.scheduler.pending-scan-interval:30000}")
    public void processPendingMessages() {
        try {
            List<LocalMessage> pendingMessages = localMessageService.getPendingMessages(properties.getScheduler().getBatchSize());
            if (pendingMessages.isEmpty()) {
                return;
            }
            
            log.info("扫描到待处理消息: count={}", pendingMessages.size());
            
            // 批量更新状态为处理中，防止重复处理
            List<Long> messageIds = pendingMessages.stream()
                    .map(LocalMessage::getId)
                    .collect(Collectors.toList());
            localMessageService.batchUpdateToProcessing(messageIds);
            
            // 异步处理消息
            for (LocalMessage message : pendingMessages) {
                CompletableFuture.runAsync(() -> {
                    try {
                        localMessageService.processMessage(message);
                    } catch (Exception e) {
                        log.error("处理待处理消息异常: messageId={}", message.getId(), e);
                    }
                }, messageProcessExecutor);
            }
        } catch (Exception e) {
            log.error("扫描待处理消息异常", e);
        }
    }
    
    /**
     * 处理需要重试的消息
     */
    @Scheduled(fixedDelayString = "${local-message.scheduler.retry-scan-interval:60000}")
    public void processRetryMessages() {
        try {
            List<LocalMessage> retryMessages = localMessageService.getRetryMessages(properties.getScheduler().getBatchSize());
            if (retryMessages.isEmpty()) {
                return;
            }
            
            log.info("扫描到重试消息: count={}", retryMessages.size());
            
            // 批量更新状态为处理中，防止重复处理
            List<Long> messageIds = retryMessages.stream()
                    .map(LocalMessage::getId)
                    .collect(Collectors.toList());
            localMessageService.batchUpdateToProcessing(messageIds);
            
            // 异步处理消息
            for (LocalMessage message : retryMessages) {
                CompletableFuture.runAsync(() -> {
                    try {
                        localMessageService.processMessage(message);
                    } catch (Exception e) {
                        log.error("处理重试消息异常: messageId={}", message.getId(), e);
                    }
                }, messageProcessExecutor);
            }
        } catch (Exception e) {
            log.error("扫描重试消息异常", e);
        }
    }
}
