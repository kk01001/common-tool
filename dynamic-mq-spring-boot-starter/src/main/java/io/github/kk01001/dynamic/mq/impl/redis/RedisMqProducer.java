package io.github.kk01001.dynamic.mq.impl.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.core.AbstractMqProducer;
import io.github.kk01001.dynamic.mq.core.AdvancedMqProducer;
import io.github.kk01001.dynamic.mq.core.MqSendCallback;
import io.github.kk01001.dynamic.mq.core.MqTransactionListener;
import io.github.kk01001.dynamic.mq.enums.TransactionState;
import io.github.kk01001.dynamic.mq.model.MqMessage;
import io.github.kk01001.dynamic.mq.model.MqSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 基于Redisson的Redis消息队列生产者实现
 */
@Slf4j
@RequiredArgsConstructor
public class RedisMqProducer extends AbstractMqProducer implements AdvancedMqProducer {

    private final DynamicMqProperties properties;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    
    @PostConstruct
    public void init() {
        log.info("Redisson生产者初始化成功");
    }
    
    @PreDestroy
    public void destroy() {
        log.info("Redisson生产者资源清理完成");
    }
    
    @Override
    protected boolean doSend(MqMessage message) {
        try {
            String queueName = buildQueueName(message.getTopic());
            String messageJson = objectMapper.writeValueAsString(message);

            // 检查是否是延迟消息
            if (message.getDelayTime() != null && message.getDelayTime() > 0) {
                // 发送延迟消息
                RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(
                        redissonClient.getQueue(queueName));
                delayedQueue.offer(messageJson, message.getDelayTime(), TimeUnit.MILLISECONDS);

                log.debug("Redisson延迟消息发送成功: queue={}, delay={}ms, messageId={}",
                        queueName, message.getDelayTime(), message.getMessageId());
            } else {
                // 发送普通消息
                RQueue<String> queue = redissonClient.getQueue(queueName);
                boolean success = queue.offer(messageJson);

                if (success) {
                    log.debug("Redisson消息发送成功: queue={}, messageId={}", queueName, message.getMessageId());
                }
                return success;
            }

            return true;
        } catch (Exception e) {
            log.error("Redisson发送消息失败", e);
            return false;
        }
    }
    
    @Override
    protected boolean doBatchSend(MqMessage... messages) {
        try {
            // 使用批量操作提高性能
            RBatch batch = redissonClient.createBatch();

            for (MqMessage message : messages) {
                String queueName = buildQueueName(message.getTopic());
                String messageJson = objectMapper.writeValueAsString(message);
                batch.getQueue(queueName).offerAsync(messageJson);
            }

            // 执行批量操作
            batch.execute();
            return true;
        } catch (Exception e) {
            log.error("Redisson批量发送消息失败", e);
            return false;
        }
    }

    // ==================== 新增高级功能实现 ====================

    @Override
    public void sendAsync(MqMessage message, MqSendCallback callback) {
        try {
            String queueName = buildQueueName(message.getTopic());
            RQueue<String> queue = redissonClient.getQueue(queueName);
            String messageJson = objectMapper.writeValueAsString(message);

            // 异步发送
            RFuture<Boolean> future = queue.offerAsync(messageJson);
            future.whenComplete((success, throwable) -> {
                if (throwable != null) {
                    callback.onException(throwable);
                } else if (success) {
                    MqSendResult result = MqSendResult.builder()
                            .messageId(message.getMessageId())
                            .topic(message.getTopic())
                            .status("SUCCESS")
                            .queueId(queueName)
                            .sendTimestamp(System.currentTimeMillis())
                            .build();
                    callback.onSuccess(result);
                } else {
                    callback.onException(new RuntimeException("消息发送失败"));
                }
            });
        } catch (Exception e) {
            callback.onException(e);
        }
    }

    @Override
    public void sendOneWay(MqMessage message) {
        try {
            String queueName = buildQueueName(message.getTopic());
            RQueue<String> queue = redissonClient.getQueue(queueName);
            String messageJson = objectMapper.writeValueAsString(message);

            // OneWay发送：不等待结果
            queue.offerAsync(messageJson);
        } catch (Exception e) {
            log.warn("Redisson OneWay发送失败，忽略异常: {}", e.getMessage());
        }
    }

    @Override
    public boolean sendOrderly(MqMessage message, String orderKey) {
        try {
            // 使用分布式锁保证顺序
            String lockKey = "order_lock:" + message.getTopic() + ":" + orderKey;
            RLock lock = redissonClient.getLock(lockKey);

            try {
                // 尝试获取锁，最多等待1秒，锁定10秒
                if (lock.tryLock(1, 10, TimeUnit.SECONDS)) {
                    String queueName = buildOrderQueueName(message.getTopic(), orderKey);
                    RQueue<String> queue = redissonClient.getQueue(queueName);
                    String messageJson = objectMapper.writeValueAsString(message);

                    boolean success = queue.offer(messageJson);
                    log.debug("Redisson顺序消息发送成功: queue={}, orderKey={}, messageId={}",
                            queueName, orderKey, message.getMessageId());
                    return success;
                } else {
                    log.warn("获取顺序消息锁失败: orderKey={}", orderKey);
                    return false;
                }
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("Redisson发送顺序消息失败", e);
            return false;
        }
    }



    @Override
    public boolean sendTransaction(MqMessage message, String transactionId, MqTransactionListener transactionListener) {
        try {
            // 执行本地事务
            TransactionState localState = transactionListener.executeLocalTransaction(message, transactionId);

            if (localState == TransactionState.COMMIT) {
                // 提交：发送消息
                return doSend(message);
            } else {
                // 回滚：不发送消息
                log.info("事务回滚，不发送消息: transactionId={}", transactionId);
                return false;
            }
        } catch (Exception e) {
            log.error("Redisson发送事务消息失败", e);
            return false;
        }
    }

    @Override
    public String getProducerType() {
        return "redis";
    }

    // ==================== AdvancedMqProducer 接口实现 ====================

    @Override
    public boolean supportsAsync() {
        return true;
    }

    @Override
    public boolean supportsOneWay() {
        return true;
    }

    @Override
    public boolean supportsOrderly() {
        return true; // 通过分布式锁实现
    }

    @Override
    public boolean supportsTransaction() {
        return true; // 通过本地事务实现
    }

    @Override
    public boolean supportsDelay() {
        return true; // Redisson原生支持延迟队列
    }

    @Override
    public long[] getSupportedDelayLevels() {
        // Redisson支持任意延迟时间
        return new long[]{1000, 5000, 10000, 30000, 60000, 120000, 180000, 240000, 300000, 600000};
    }

    @Override
    public int getMaxBatchSize() {
        return 1000; // Redisson批量操作建议大小
    }

    // ==================== 私有方法 ====================

    private String buildQueueName(String topic) {
        return "mq:queue:" + topic;
    }

    private String buildOrderQueueName(String topic, String orderKey) {
        return "mq:order:" + topic + ":" + orderKey;
    }
}
