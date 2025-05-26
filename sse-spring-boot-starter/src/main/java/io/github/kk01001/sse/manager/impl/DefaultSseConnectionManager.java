package io.github.kk01001.sse.manager.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.github.kk01001.sse.config.SseProperties;
import io.github.kk01001.sse.manager.SseConnectionManager;
import io.github.kk01001.sse.model.SseMessage;
import io.github.kk01001.sse.publisher.SseMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 默认SSE连接管理器实现
 */
@Slf4j
public class DefaultSseConnectionManager implements SseConnectionManager {

    /**
     * 客户端连接映射表 clientId -> SseEmitter
     */
    private final Map<String, SseEmitter> clientEmitters = new ConcurrentHashMap<>();

    /**
     * 用户连接映射表 userId -> Set<clientId>
     */
    private final Map<String, Set<String>> userClients = new ConcurrentHashMap<>();

    /**
     * 主题连接映射表 topic -> Set<clientId>
     */
    private final Map<String, Set<String>> topicClients = new ConcurrentHashMap<>();

    /**
     * 客户端信息映射表 clientId -> (userId, topic)
     */
    private final Map<String, ClientInfo> clientInfoMap = new ConcurrentHashMap<>();

    /**
     * 心跳定时器
     */
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * SSE配置属性
     */
    private final SseProperties properties;

    /**
     * 消息发布者
     */
    private final SseMessagePublisher messagePublisher;

    public DefaultSseConnectionManager(SseProperties properties, SseMessagePublisher messagePublisher) {
        this.properties = properties;
        this.messagePublisher = messagePublisher;
        // 启动心跳任务
        startHeartbeat();
    }

    /**
     * 启动心跳任务
     */
    private void startHeartbeat() {
        if (properties.getHeartbeatInterval() > 0) {
            heartbeatExecutor.scheduleAtFixedRate(() -> {
                try {
                    sendHeartbeat();
                } catch (Exception e) {
                    log.error("发送心跳消息异常", e);
                }
            }, properties.getHeartbeatInterval(), properties.getHeartbeatInterval(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 发送心跳消息
     */
    private void sendHeartbeat() {
        clientEmitters.forEach((clientId, emitter) -> {
            try {
                SseMessage heartbeatMessage = SseMessage.builder()
                        .id(IdUtil.fastSimpleUUID())
                        .data("heartbeat")
                        .heartbeat(true)
                        .build();
                emitter.send(heartbeatMessage, MediaType.APPLICATION_JSON);
            } catch (IOException e) {
                log.error("发送心跳消息失败，clientId: {}", clientId, e);
                closeConnection(clientId);
            }
        });
    }

    @Override
    public SseEmitter createConnection(String clientId, String userId, String topic) {
        // 如果clientId为空，则生成一个
        if (StrUtil.isBlank(clientId)) {
            clientId = IdUtil.fastSimpleUUID();
        }

        // 如果已存在相同clientId的连接，先关闭旧连接
        if (clientEmitters.containsKey(clientId)) {
            closeConnection(clientId);
        }

        // 创建新的SSE连接
        SseEmitter emitter = new SseEmitter(properties.getClientTimeout());
        clientEmitters.put(clientId, emitter);

        // 保存客户端信息
        ClientInfo clientInfo = new ClientInfo(userId, topic);
        clientInfoMap.put(clientId, clientInfo);

        // 关联用户和客户端
        if (StrUtil.isNotBlank(userId)) {
            userClients.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(clientId);
        }

        // 关联主题和客户端
        if (StrUtil.isNotBlank(topic)) {
            topicClients.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(clientId);
        }

        // 设置连接完成回调
        String finalClientId = clientId;
        emitter.onCompletion(() -> {
            log.debug("SSE连接完成，clientId: {}", finalClientId);
            closeConnection(finalClientId);
        });

        // 设置连接超时回调
        emitter.onTimeout(() -> {
            log.debug("SSE连接超时，clientId: {}", finalClientId);
            closeConnection(finalClientId);
        });

        // 设置连接错误回调
        emitter.onError(throwable -> {
            log.error("SSE连接异常，clientId: {}", finalClientId, throwable);
            closeConnection(finalClientId);
        });

        try {
            // 发送连接成功消息
            emitter.send(SseEmitter.event()
                    .id(clientId)
                    .name("connect")
                    .data("连接成功", MediaType.TEXT_PLAIN));
            log.debug("SSE连接创建成功，clientId: {}, userId: {}, topic: {}", clientId, userId, topic);
        } catch (IOException e) {
            log.error("SSE连接创建失败，clientId: {}", clientId, e);
            closeConnection(clientId);
        }

        return emitter;
    }

    @Override
    public void closeConnection(String clientId) {
        if (StrUtil.isBlank(clientId)) {
            return;
        }

        // 获取客户端信息
        ClientInfo clientInfo = clientInfoMap.get(clientId);
        if (clientInfo != null) {
            // 移除用户关联
            if (StrUtil.isNotBlank(clientInfo.userId())) {
                Set<String> userClientIds = userClients.get(clientInfo.userId());
                if (CollUtil.isNotEmpty(userClientIds)) {
                    userClientIds.remove(clientId);
                    if (userClientIds.isEmpty()) {
                        userClients.remove(clientInfo.userId());
                    }
                }
            }

            // 移除主题关联
            if (StrUtil.isNotBlank(clientInfo.topic())) {
                Set<String> topicClientIds = topicClients.get(clientInfo.topic());
                if (CollUtil.isNotEmpty(topicClientIds)) {
                    topicClientIds.remove(clientId);
                    if (topicClientIds.isEmpty()) {
                        topicClients.remove(clientInfo.topic());
                    }
                }
            }

            // 移除客户端信息
            clientInfoMap.remove(clientId);
        }

        // 获取并移除SSE连接
        SseEmitter emitter = clientEmitters.remove(clientId);
        if (emitter != null) {
            try {
                emitter.complete();
                log.debug("SSE连接已关闭，clientId: {}", clientId);
            } catch (Exception e) {
                log.error("关闭SSE连接异常，clientId: {}", clientId, e);
            }
        }
    }

    @Override
    public void sendMessage(SseMessage message) {
        if (message == null) {
            return;
        }

        // 如果开启了集群模式，则发布消息到集群
        if (properties.isCluster() && !Boolean.TRUE.equals(message.getHeartbeat())) {
            messagePublisher.publishMessage(message);
            return;
        }

        // 根据消息类型选择发送方式
        if (StrUtil.isNotBlank(message.getClientId())) {
            // 发送给指定客户端
            sendMessageToClient(message.getClientId(), message);
            return;
        }
        if (StrUtil.isNotBlank(message.getUserId())) {
            // 发送给指定用户
            sendMessageToUser(message.getUserId(), message);
            return;
        }
        if (StrUtil.isNotBlank(message.getTopic())) {
            // 发送给指定主题
            sendMessageToTopic(message.getTopic(), message);
            return;
        }
        if (Boolean.TRUE.equals(message.getBroadcast())) {
            // 广播消息
            broadcastMessage(message);
        }
    }

    @Override
    public void sendMessageToClient(String clientId, SseMessage message) {
        if (StrUtil.isBlank(clientId) || message == null) {
            return;
        }

        SseEmitter emitter = clientEmitters.get(clientId);
        if (emitter != null) {
            try {
                if (StrUtil.isNotBlank(message.getEvent())) {
                    // 发送带事件名的消息
                    emitter.send(SseEmitter.event()
                            .id(message.getId())
                            .name(message.getEvent())
                            .data(message.getData(), MediaType.APPLICATION_JSON)
                            .reconnectTime(message.getRetry() != null ? message.getRetry() : 30000));
                } else {
                    // 发送普通消息
                    emitter.send(message, MediaType.APPLICATION_JSON);
                }
                log.debug("消息发送成功，clientId: {}, messageId: {}", clientId, message.getId());
            } catch (IOException e) {
                log.error("消息发送失败，clientId: {}, messageId: {}", clientId, message.getId(), e);
                closeConnection(clientId);
            }
        } else {
            log.warn("客户端不存在，clientId: {}", clientId);
        }
    }

    @Override
    public void sendMessageToUser(String userId, SseMessage message) {
        if (StrUtil.isBlank(userId) || message == null) {
            return;
        }

        Set<String> clientIds = userClients.get(userId);
        if (CollUtil.isNotEmpty(clientIds)) {
            for (String clientId : clientIds) {
                sendMessageToClient(clientId, message);
            }
        } else {
            log.warn("用户没有活跃连接，userId: {}", userId);
        }
    }

    @Override
    public void sendMessageToTopic(String topic, SseMessage message) {
        if (StrUtil.isBlank(topic) || message == null) {
            return;
        }

        Set<String> clientIds = topicClients.get(topic);
        if (CollUtil.isNotEmpty(clientIds)) {
            for (String clientId : clientIds) {
                sendMessageToClient(clientId, message);
            }
        } else {
            log.warn("主题没有活跃连接，topic: {}", topic);
        }
    }

    @Override
    public void broadcastMessage(SseMessage message) {
        if (message == null) {
            return;
        }

        clientEmitters.keySet().forEach(clientId -> sendMessageToClient(clientId, message));
    }

    @Override
    public int getConnectionCount() {
        return clientEmitters.size();
    }

    /**
     * 客户端信息
     */
    private record ClientInfo(String userId, String topic) {

    }
}
