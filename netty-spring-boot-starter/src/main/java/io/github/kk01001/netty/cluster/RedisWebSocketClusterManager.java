package io.github.kk01001.netty.cluster;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.netty.cluster.model.BroadcastMessage;
import io.github.kk01001.netty.cluster.model.SessionInfo;
import io.github.kk01001.netty.config.NettyWebSocketProperties;
import io.github.kk01001.netty.session.WebSocketSession;
import io.netty.util.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisWebSocketClusterManager implements WebSocketClusterManager {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NettyWebSocketProperties properties;
    private final RedisMessageListenerContainer listenerContainer;
    private final ClusterMessageHandler messageHandler;
    private final String nodeId;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> heartbeatFuture;

    private final String sessionKeyPrefix;
    private final String nodeKeyPrefix;
    private final String broadcastChannelPrefix;

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

        // 从配置中获取前缀
        this.sessionKeyPrefix = properties.getCluster().getSessionKeyPrefix();
        this.nodeKeyPrefix = properties.getCluster().getNodeKeyPrefix();
        this.broadcastChannelPrefix = properties.getCluster().getBroadcastChannelPrefix();
    }
    
    @Override
    public void init() {
        // 订阅广播消息
        subscribeToChannels();
        // 注册节点
        registerNode();
        // 启动心跳
        startHeartbeat();
        // 启动清理任务
        startCleanupTask();
        log.info("Redis集群管理器初始化完成, nodeId: {}", nodeId);
    }
    
    @Override
    public void addSession(WebSocketSession session) {
        String sessionKey = getSessionKey(session.getId());
        SessionInfo sessionInfo = new SessionInfo(session, nodeId);
        try {
            String json = objectMapper.writeValueAsString(sessionInfo);
            redisTemplate.opsForHash().put(sessionKey, session.getId(), json);
            log.debug("添加会话到Redis: {}, sessionId: {}", sessionKey, session.getId());
        } catch (Exception e) {
            log.error("保存会话信息到Redis失败: {}", sessionKey, e);
            throw new RuntimeException("保存会话信息失败", e);
        }
    }
    
    @Override
    public void removeSession(String sessionId) {
        String sessionKey = getSessionKey(sessionId);
        try {
            redisTemplate.opsForHash().delete(sessionKey, sessionId);
            log.debug("从Redis移除会话: {}", sessionKey);
        } catch (Exception e) {
            log.error("从Redis删除会话失败: {}", sessionKey, e);
        }
    }
    
    @Override
    public void broadcast(String message, String targetSessionId) {
        if (!StringUtils.hasText(message)) {
            return;
        }
        
        try {
            BroadcastMessage broadcastMessage = new BroadcastMessage(message, nodeId, targetSessionId);
            String json = objectMapper.writeValueAsString(broadcastMessage);
            String channel = getBroadcastChannel();
            redisTemplate.convertAndSend(channel, json);
            log.debug("发送广播消息到Redis: channel={}, targetSessionId={}, message={}", channel, targetSessionId, json);
        } catch (Exception e) {
            log.error("targetSessionId={}, message={}, 发送广播消息到Redis失败:", targetSessionId, message, e);
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
            // 移除自身节点
            unregisterNode();
            // 清理本节点的会话数据
            cleanupSessions();
            log.info("Redis集群管理器已销毁, nodeId: {}", nodeId);
        } catch (Exception e) {
            log.error("销毁Redis集群管理器失败", e);
        }
    }
    
    private void subscribeToChannels() {
        // 订阅全局广播
        listenerContainer.addMessageListener(
                (message, pattern) -> handleBroadcastLocalMessage(message.toString()),
                new PatternTopic(broadcastChannelPrefix + "*")
        );
    }

    private void handleBroadcastLocalMessage(String messageJson) {
        try {
            BroadcastMessage message = objectMapper.readValue(messageJson, BroadcastMessage.class);
            String targetSessionId = message.getTargetSessionId();
            if (Objects.equals(nodeId, message.getNodeId())) {
                log.debug("nodeId: {}, 忽略本节点的消息: {}", nodeId, messageJson);
                return;
            }

            // 私聊消息
            if (StringUtils.hasText(targetSessionId)) {
                messageHandler.handlePrivateMessage(targetSessionId, message.getMessage(), true);
                log.debug("私聊消息: {}", messageJson);
                return;
            }
            // 广播消息 给本机每个会话发送
            messageHandler.handleBroadcastLocalMessage(message.getMessage());
            log.debug("处理本机广播消息: {}", messageJson);
        } catch (Exception e) {
            log.error("处理广播消息失败: {}", messageJson, e);
        }
    }
    
    private void registerNode() {
        String nodeKey = getNodeKey();
        try {
            redisTemplate.opsForHash().put(nodeKey, nodeId, String.valueOf(System.currentTimeMillis()));
            messageHandler.handleNodeEvent(nodeId, ClusterMessageHandler.NodeEvent.NODE_UP);
            log.info("注册节点到Redis: {}", nodeKey);
        } catch (Exception e) {
            log.error("注册节点到Redis失败: {}", nodeKey, e);
            throw new RuntimeException("注册节点失败", e);
        }
    }
    
    private void unregisterNode() {
        String nodeKey = getNodeKey();
        try {
            redisTemplate.opsForHash().delete(nodeKey, nodeId);
            messageHandler.handleNodeEvent(nodeId, ClusterMessageHandler.NodeEvent.NODE_DOWN);
            log.info("从Redis注销节点: {}", nodeKey);
        } catch (Exception e) {
            log.error("从Redis注销节点失败: {}", nodeKey, e);
        }
    }
    
    private void startHeartbeat() {
        heartbeatFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                String nodeKey = getNodeKey();
                redisTemplate.opsForHash().put(nodeKey, nodeId, String.valueOf(System.currentTimeMillis()));
                log.debug("更新节点心跳: {}, nodeId: {}", nodeKey, nodeId);
            } catch (Exception e) {
                log.error("更新节点心跳失败", e);
            }
        }, 0, properties.getCluster().getSessionTimeout().toSeconds() / 5, TimeUnit.SECONDS);
    }

    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupDeadNodes();
            } catch (Exception e) {
                log.error("清理死亡节点任务失败", e);
            }
        }, 0, properties.getCluster().getCleanupInterval().toSeconds(), TimeUnit.SECONDS);
    }

    private void cleanupDeadNodes() {
        String nodeKey = getNodeKey();
        try {
            Map<Object, Object> nodeData = redisTemplate.opsForHash().entries(nodeKey);
            if (nodeData.isEmpty()) {
                return;
            }

            long now = System.currentTimeMillis();
            nodeData.forEach((nodeId, lastHeartbeatStr) -> {
                try {
                    long lastHeartbeat = Long.parseLong((String) lastHeartbeatStr);
                    if (now - lastHeartbeat > properties.getCluster().getSessionTimeout().toMillis()) {
                        // 清理节点
                        redisTemplate.opsForHash().delete(nodeKey, nodeId);
                        messageHandler.handleNodeEvent((String) nodeId, ClusterMessageHandler.NodeEvent.NODE_TIMEOUT);
                        log.info("清理死亡节点: {}", nodeId);

                        // 清理该节点的会话数据
                        cleanupSessionsForNode((String) nodeId);
                    }
                } catch (Exception e) {
                    log.error("清理死亡节点失败: {}", nodeId, e);
                }
            });
        } catch (Exception e) {
            log.error("清理死亡节点任务失败", e);
        }
    }
    
    private void cleanupSessions() {
        cleanupSessionsForNode(this.nodeId);
    }

    private void cleanupSessionsForNode(String nodeId) {
        int shardCount = properties.getCluster().getSessionShardCount();
        for (int shard = 0; shard < shardCount; shard++) {
            String sessionKey = sessionKeyPrefix + nodeId + ":" + shard;
            try {
                Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(sessionKey);
                if (!sessionData.isEmpty()) {
                    redisTemplate.unlink(sessionKey);
                    log.info("清理节点 {} 的会话分片: {}", nodeId, sessionKey);
                }
            } catch (Exception e) {
                log.error("清理节点 {} 的会话分片失败: {}", nodeId, sessionKey, e);
            }
        }
    }
    
    private String generateNodeId() {
        return NetUtil.LOCALHOST4.getHostAddress() + "-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String getSessionKey(String sessionId) {
        int shard = Math.abs(sessionId.hashCode()) % properties.getCluster().getSessionShardCount();
        return sessionKeyPrefix + nodeId + ":" + shard;
    }

    private String getNodeKey() {
        return nodeKeyPrefix;
    }

    private String getBroadcastChannel() {
        return broadcastChannelPrefix;
    }
} 