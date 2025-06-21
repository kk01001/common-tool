package io.github.kk01001.dynamic.mq.impl.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.core.AbstractMqConsumer;
import io.github.kk01001.dynamic.mq.core.MqDeadLetterHandler;
import io.github.kk01001.dynamic.mq.core.MqMessageFilter;
import io.github.kk01001.dynamic.mq.core.MqRetryStrategy;
import io.github.kk01001.dynamic.mq.enums.ConsumeMode;
import io.github.kk01001.dynamic.mq.model.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 基于Redisson的Redis消息队列消费者实现
 */
@Slf4j
@RequiredArgsConstructor
public class RedisMqConsumer extends AbstractMqConsumer {

    private final DynamicMqProperties properties;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private ExecutorService executorService;
    private final Map<String, Boolean> runningConsumers = new ConcurrentHashMap<>();
    private final Map<String, MqMessageHandler> messageHandlers = new ConcurrentHashMap<>();

    // 高级功能组件
    private MqRetryStrategy retryStrategy;
    private MqDeadLetterHandler deadLetterHandler;
    private MqMessageFilter messageFilter;
    
    @Override
    protected void doStart() {
        try {
            initAdvancedFeatures();
            initExecutorService();
            log.info("Redisson消费者已启动");
        } catch (Exception e) {
            log.error("Redisson消费者启动失败", e);
            throw new RuntimeException("Redisson消费者启动失败", e);
        }
    }
    
    @Override
    protected void doStop() {
        // 停止所有消费者
        runningConsumers.replaceAll((k, v) -> false);

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (redissonClient != null && !redissonClient.isShutdown()) {
            redissonClient.shutdown();
        }

        runningConsumers.clear();
        messageHandlers.clear();
        log.info("Redisson消费者已停止");
    }
    
    @Override
    protected void doSubscribe(String topic, String tag, MqMessageHandler messageHandler) {
        try {
            String key = topic + ":" + (tag != null && !tag.trim().isEmpty() ? tag : "");
            String queueName = buildQueueName(topic);

            messageHandlers.put(key, messageHandler);
            runningConsumers.put(key, true);

            // 根据消费模式启动消费者
            if (properties.getConsumer().getConsumeMode() == ConsumeMode.ORDERLY) {
                startOrderlyConsumer(key, queueName, tag, messageHandler);
            } else {
                startConcurrentConsumer(key, queueName, tag, messageHandler);
            }

            log.info("Redisson订阅成功: topic={}, tag={}, queue={}", topic, tag, queueName);
        } catch (Exception e) {
            log.error("Redisson订阅失败: topic={}, tag={}", topic, tag, e);
            throw new RuntimeException("Redisson订阅失败", e);
        }
    }
    
    @Override
    protected void doUnsubscribe(String topic) {
        runningConsumers.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(topic + ":")) {
                entry.setValue(false); // 停止消费者
                return true;
            }
            return false;
        });
        messageHandlers.entrySet().removeIf(entry -> entry.getKey().startsWith(topic + ":"));
        log.info("Redisson取消订阅: topic={}", topic);
    }

    @Override
    public String getConsumerType() {
        return "redis";
    }
    


    /**
     * 初始化高级功能组件
     */
    private void initAdvancedFeatures() {
        // 初始化重试策略
        if (properties.getConsumer().getRetry().isEnabled()) {
            String strategy = properties.getConsumer().getRetry().getStrategy();
            switch (strategy) {
                case "fixed":
                    this.retryStrategy = MqRetryStrategy.fixedDelay(
                            properties.getConsumer().getRetry().getMaxRetryTimes(),
                            properties.getConsumer().getRetry().getFixedDelay()
                    );
                    break;
                case "exponential":
                    this.retryStrategy = MqRetryStrategy.exponentialBackoff(
                            properties.getConsumer().getRetry().getMaxRetryTimes(),
                            properties.getConsumer().getRetry().getInitialDelay(),
                            properties.getConsumer().getRetry().getMultiplier()
                    );
                    break;
                default:
                    this.retryStrategy = MqRetryStrategy.fixedDelay(3, 1000L);
            }
        }

        // 初始化死信队列处理器
        if (properties.getConsumer().getDeadLetter().isEnabled()) {
            String handler = properties.getConsumer().getDeadLetter().getHandler();
            switch (handler) {
                case "logging":
                    this.deadLetterHandler = MqDeadLetterHandler.loggingHandler();
                    break;
                case "default":
                default:
                    this.deadLetterHandler = MqDeadLetterHandler.defaultHandler();
            }
        }

        // 初始化消息过滤器
        if (properties.getConsumer().getFilter().isEnabled()) {
            String filterType = properties.getConsumer().getFilter().getDefaultType();
            switch (filterType) {
                case "tag":
                    this.messageFilter = MqMessageFilter.tagFilter("*");
                    break;
                case "sql":
                    this.messageFilter = MqMessageFilter.sqlFilter("1=1");
                    break;
                default:
                    this.messageFilter = MqMessageFilter.tagFilter("*");
            }
        }
    }

    /**
     * 初始化线程池
     */
    private void initExecutorService() {
        int threadCount = Math.max(properties.getConsumer().getConsumeThreadMax(),
                                 Runtime.getRuntime().availableProcessors());
        this.executorService = Executors.newFixedThreadPool(threadCount, r -> {
            Thread thread = new Thread(r, "redisson-consumer-" + System.currentTimeMillis());
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 启动并发消费者
     */
    private void startConcurrentConsumer(String key, String queueName, String tag, MqMessageHandler messageHandler) {
        int consumerCount = properties.getConsumer().getConsumeThreadMax();

        for (int i = 0; i < consumerCount; i++) {
            final int consumerId = i;
            executorService.submit(() -> {
                RBlockingQueue<String> queue = redissonClient.getBlockingQueue(queueName);

                while (runningConsumers.getOrDefault(key, false)) {
                    try {
                        // 阻塞获取消息，超时时间1秒
                        String messageJson = queue.poll(1, TimeUnit.SECONDS);
                        if (messageJson != null) {
                            MqMessage mqMessage = objectMapper.readValue(messageJson, MqMessage.class);

                            // 消息过滤
                            if (shouldFilterMessage(mqMessage, tag)) {
                                continue;
                            }

                            // 处理消息
                            handleMessageWithAdvancedFeatures(mqMessage, messageHandler, key, consumerId);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("Redisson并发消费者[{}]处理消息失败", consumerId, e);
                        try {
                            Thread.sleep(1000); // 避免快速失败循环
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }

                log.info("Redisson并发消费者[{}]已停止: key={}", consumerId, key);
            });
        }
    }

    /**
     * 启动顺序消费者
     */
    private void startOrderlyConsumer(String key, String queueName, String tag, MqMessageHandler messageHandler) {
        executorService.submit(() -> {
            RBlockingQueue<String> queue = redissonClient.getBlockingQueue(queueName);

            while (runningConsumers.getOrDefault(key, false)) {
                try {
                    // 阻塞获取消息，超时时间1秒
                    String messageJson = queue.poll(1, TimeUnit.SECONDS);
                    if (messageJson != null) {
                        MqMessage mqMessage = objectMapper.readValue(messageJson, MqMessage.class);

                        // 消息过滤
                        if (shouldFilterMessage(mqMessage, tag)) {
                            continue;
                        }

                        // 顺序处理消息
                        handleMessageWithAdvancedFeatures(mqMessage, messageHandler, key, 0);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Redisson顺序消费者处理消息失败", e);
                    try {
                        Thread.sleep(1000); // 避免快速失败循环
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            log.info("Redisson顺序消费者已停止: key={}", key);
        });
    }



    /**
     * 判断是否应该过滤消息
     */
    private boolean shouldFilterMessage(MqMessage mqMessage, String filterTag) {
        // Tag过滤
        if (filterTag != null && !filterTag.trim().isEmpty() && !filterTag.equals("*")) {
            if (!filterTag.equals(mqMessage.getTag())) {
                return true;
            }
        }

        // 高级过滤器
        if (messageFilter != null && !messageFilter.filter(mqMessage)) {
            return true;
        }

        return false;
    }

    /**
     * 增强的消息处理（支持重试、死信队列、过滤等）
     */
    private void handleMessageWithAdvancedFeatures(MqMessage mqMessage, MqMessageHandler messageHandler,
                                                   String key, int consumerId) {
        int retryCount = 0;
        boolean success = false;
        Exception lastException = null;

        try {
            success = messageHandler.handle(mqMessage);
        } catch (Exception e) {
            lastException = e;
            log.error("处理消息失败: messageId={}, consumerId={}", mqMessage.getMessageId(), consumerId, e);
        }

        if (success) {
            log.debug("消息处理成功: messageId={}, consumerId={}", mqMessage.getMessageId(), consumerId);
        } else {
            // 消息处理失败，判断是否需要重试
            if (retryStrategy != null && retryStrategy.shouldRetry(mqMessage, retryCount, lastException)) {
                // 重试：发送到重试队列
                long delay = retryStrategy.getRetryDelay(retryCount + 1);
                sendToRetryQueue(mqMessage, retryCount + 1, delay);
            } else {
                // 达到最大重试次数，发送到死信队列
                if (deadLetterHandler != null) {
                    deadLetterHandler.handleDeadLetter(mqMessage, mqMessage.getTopic(),
                            properties.getConsumer().getGroupName(), retryCount, lastException);
                }
            }
        }
    }

    /**
     * 发送消息到重试队列
     */
    private void sendToRetryQueue(MqMessage mqMessage, int retryCount, long delay) {
        try {
            String retryQueueName = buildRetryQueueName(mqMessage.getTopic());
            RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(
                    redissonClient.getQueue(retryQueueName));

            // 设置重试次数
            mqMessage.getHeaders().put("retryCount", retryCount);
            String messageJson = objectMapper.writeValueAsString(mqMessage);

            // 发送到延迟队列
            delayedQueue.offer(messageJson, delay, TimeUnit.MILLISECONDS);

            log.info("消息发送到重试队列: messageId={}, retryCount={}, delay={}ms",
                    mqMessage.getMessageId(), retryCount, delay);

        } catch (Exception e) {
            log.error("发送消息到重试队列失败", e);
        }
    }

    /**
     * 构建队列名称
     */
    private String buildQueueName(String topic) {
        return "mq:queue:" + topic;
    }

    /**
     * 构建重试队列名称
     */
    private String buildRetryQueueName(String topic) {
        return "mq:retry:" + topic;
    }
}
