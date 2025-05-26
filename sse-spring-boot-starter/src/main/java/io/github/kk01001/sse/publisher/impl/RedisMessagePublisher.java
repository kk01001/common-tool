package io.github.kk01001.sse.publisher.impl;

import com.alibaba.fastjson.JSON;
import io.github.kk01001.sse.config.SseProperties;
import io.github.kk01001.sse.model.SseMessage;
import io.github.kk01001.sse.publisher.SseMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 基于Redis的消息发布者实现
 */
@Slf4j
public class RedisMessagePublisher implements SseMessagePublisher {

    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * SSE配置属性
     */
    private final SseProperties properties;

    public RedisMessagePublisher(RedisTemplate<String, Object> redisTemplate, SseProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public void publishMessage(SseMessage message) {
        if (message == null) {
            return;
        }

        try {
            String messageJson = JSON.toJSONString(message);
            redisTemplate.convertAndSend(properties.getRedis().getTopic(), messageJson);
            log.debug("Redis发布消息成功: {}", messageJson);
        } catch (Exception e) {
            log.error("Redis发布消息失败", e);
        }
    }
}
