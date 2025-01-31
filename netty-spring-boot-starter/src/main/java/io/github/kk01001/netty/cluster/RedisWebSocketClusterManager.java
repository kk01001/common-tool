package io.github.kk01001.netty.cluster;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.netty.config.NettyWebSocketProperties;
import io.github.kk01001.netty.session.WebSocketSession;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisWebSocketClusterManager implements WebSocketClusterManager {
    
    private static final String SESSION_KEY_PREFIX = "ws:session:";
    private static final String NODE_KEY_PREFIX = "ws:node:";
    private static final String BROADCAST_CHANNEL_PREFIX = "ws:broadcast:";
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NettyWebSocketProperties properties;
    private final RedisMessageListenerContainer listenerContainer;
    private final ClusterMessageHandler messageHandler;
    private final String nodeId;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> heartbeatFuture;
    
    public RedisWebSocketClusterManager(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            NettyWebSocketProperties properties,
            RedisMessageListenerContainer listenerContainer,
            ClusterMessageHandler messageHandler,
            ScheduledExecutorService scheduler) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.listenerContainer = listenerContainer;
        this.messageHandler = messageHandler;
        this.scheduler = scheduler;
        this.nodeId = generateNodeId();
    }
    
    @Override
    public void init() {
        // 订阅广播消息
        subscribeToChannels();
        // 注册节点
        registerNode();
        // 启动心跳
        startHeartbeat();
        log.info("Redis集群管理器初始化完成, nodeId: {}", nodeId);
    }
    
    @Override
    public void addSession(String path, WebSocketSession session) {
        String sessionKey = getSessionKey(path, session.getId());
        SessionInfo sessionInfo = new SessionInfo(session.getId(), path, nodeId);
        try {
            String json = objectMapper.writeValueAsString(sessionInfo);
            redisTemplate.opsForValue().set(sessionKey, json,
                    properties.getCluster().getSessionTimeout());
            log.debug("添加会话到Redis: {}", sessionKey);
        } catch (Exception e) {
            log.error("保存会话信息到Redis失败: {}", sessionKey, e);
            throw new RuntimeException("保存会话信息失败", e);
        }
    }
    
    @Override
    public void removeSession(String path, String sessionId) {
        String sessionKey = getSessionKey(path, sessionId);
        try {
            redisTemplate.delete(sessionKey);
            log.debug("从Redis移除会话: {}", sessionKey);
        } catch (Exception e) {
            log.error("从Redis删除会话失败: {}", sessionKey, e);
        }
    }
    
    @Override
    public void broadcast(String path, String message, String sourceNodeId) {
        if (!StringUtils.hasText(message)) {
            return;
        }
        
        try {
            BroadcastMessage broadcastMessage = new BroadcastMessage(path, message, sourceNodeId);
            String json = objectMapper.writeValueAsString(broadcastMessage);
            String channel = getBroadcastChannel(path);
            redisTemplate.convertAndSend(channel, json);
            log.debug("发送广播消息到Redis: channel={}, message={}", channel, json);
        } catch (Exception e) {
            log.error("发送广播消息到Redis失败: path={}", path, e);
            throw new RuntimeException("发送广播消息失败", e);
        }
    }
    
    @Override
    public String getNodeId() {
        return nodeId;
    }
    
    @Override
    public void destroy() {
        try {
            // 停止心跳
            if (heartbeatFuture != null) {
                heartbeatFuture.cancel(true);
            }
            // 注销节点
            unregisterNode();
            // 清理会话
            cleanupSessions();
            log.info("Redis集群管理器已销毁");
        } catch (Exception e) {
            log.error("销毁Redis集群管理器失败", e);
        }
    }
    
    private void subscribeToChannels() {
        // 订阅全局广播
        listenerContainer.addMessageListener(
                (message, pattern) -> handleBroadcastMessage(message.toString()),
                new PatternTopic(BROADCAST_CHANNEL_PREFIX + "*")
        );
    }
    
    private void handleBroadcastMessage(String messageJson) {
        try {
            BroadcastMessage message = objectMapper.readValue(messageJson, BroadcastMessage.class);
            // 忽略来自自己的消息
            if (!Objects.equals(nodeId, message.getSourceNodeId())) {
                messageHandler.handleBroadcastMessage(message.getPath(), message.getMessage());
                log.debug("处理广播消息: {}", messageJson);
            }
        } catch (Exception e) {
            log.error("处理广播消息失败: {}", messageJson, e);
        }
    }
    
    private void registerNode() {
        String nodeKey = getNodeKey(nodeId);
        NodeInfo nodeInfo = new NodeInfo(nodeId, System.currentTimeMillis());
        try {
            String json = objectMapper.writeValueAsString(nodeInfo);
            redisTemplate.opsForValue().set(nodeKey, json,
                    properties.getCluster().getSessionTimeout());
            log.info("注册节点到Redis: {}", nodeKey);
        } catch (Exception e) {
            log.error("注册节点到Redis失败: {}", nodeKey, e);
            throw new RuntimeException("注册节点失败", e);
        }
    }
    
    private void unregisterNode() {
        String nodeKey = getNodeKey(nodeId);
        redisTemplate.delete(nodeKey);
        log.info("从Redis注销节点: {}", nodeKey);
    }
    
    private void startHeartbeat() {
        heartbeatFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                registerNode();
                cleanupDeadNodes();
            } catch (Exception e) {
                log.error("执行心跳任务失败", e);
            }
        }, 0, properties.getCluster().getSessionTimeout().toSeconds() / 3, TimeUnit.SECONDS);
    }
    
    private void cleanupDeadNodes() {
        Set<String> nodeKeys = redisTemplate.keys(NODE_KEY_PREFIX + "*");
        if (nodeKeys == null) return;
        
        long now = System.currentTimeMillis();
        nodeKeys.forEach(nodeKey -> {
            try {
                String json = redisTemplate.opsForValue().get(nodeKey);
                if (json != null) {
                    NodeInfo nodeInfo = objectMapper.readValue(json, NodeInfo.class);
                    if (now - nodeInfo.getLastHeartbeat() > properties.getCluster().getSessionTimeout().toMillis()) {
                        redisTemplate.delete(nodeKey);
                        log.info("清理死亡节点: {}", nodeKey);
                    }
                }
            } catch (Exception e) {
                log.error("清理死亡节点失败: {}", nodeKey, e);
            }
        });
    }
    
    private void cleanupSessions() {
        Set<String> sessionKeys = redisTemplate.keys(SESSION_KEY_PREFIX + nodeId + ":*");
        if (sessionKeys != null) {
            redisTemplate.delete(sessionKeys);
            log.info("清理节点会话数: {}", sessionKeys.size());
        }
    }
    
    private String generateNodeId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    private String getSessionKey(String path, String sessionId) {
        return SESSION_KEY_PREFIX + nodeId + ":" + path + ":" + sessionId;
    }
    
    private String getNodeKey(String nodeId) {
        return NODE_KEY_PREFIX + nodeId;
    }
    
    private String getBroadcastChannel(String path) {
        return BROADCAST_CHANNEL_PREFIX + path;
    }
    
    @Data
    static class NodeInfo {
        private String nodeId;
        private long lastHeartbeat;
        
        public NodeInfo(String nodeId, long lastHeartbeat) {
            this.nodeId = nodeId;
            this.lastHeartbeat = lastHeartbeat;
        }
    }
    
    @Data
    static class SessionInfo {
        private String sessionId;
        private String path;
        private String nodeId;
        private long createTime = System.currentTimeMillis();
        
        public SessionInfo(String sessionId, String path, String nodeId) {
            this.sessionId = sessionId;
            this.path = path;
            this.nodeId = nodeId;
        }
    }
    
    @Data
    static class BroadcastMessage {
        private String path;
        private String message;
        private String sourceNodeId;
        private long timestamp = System.currentTimeMillis();
        
        public BroadcastMessage(String path, String message, String sourceNodeId) {
            this.path = path;
            this.message = message;
            this.sourceNodeId = sourceNodeId;
        }
    }
} 