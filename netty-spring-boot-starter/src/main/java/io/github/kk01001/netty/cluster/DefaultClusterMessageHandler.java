package io.github.kk01001.netty.cluster;

import io.github.kk01001.netty.session.WebSocketSession;
import io.github.kk01001.netty.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DefaultClusterMessageHandler implements ClusterMessageHandler {
    
    private final WebSocketSessionManager sessionManager;
    
    @Override
    public void handleBroadcastMessage(String path, String message) {
        sessionManager.broadcast(path, message);
    }
    
    @Override
    public void handlePrivateMessage(String path, String targetSessionId, String message) {
        WebSocketSession session = sessionManager.getSession(path, targetSessionId);
        if (session != null && session.isActive()) {
            session.sendMessage(message);
        }
    }
    
    @Override
    public void handleNodeEvent(String nodeId, NodeEvent event) {
        switch (event) {
            case NODE_UP -> log.info("节点上线: {}", nodeId);
            case NODE_DOWN -> log.info("节点下线: {}", nodeId);
            case NODE_TIMEOUT -> log.warn("节点超时: {}", nodeId);
        }
    }
} 