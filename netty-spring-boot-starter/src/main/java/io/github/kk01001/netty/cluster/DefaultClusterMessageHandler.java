package io.github.kk01001.netty.cluster;

import io.github.kk01001.netty.message.MessageDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DefaultClusterMessageHandler implements ClusterMessageHandler {
    
    private final MessageDispatcher messageDispatcher;

    @Override
    public void handleBroadcastMessage(String path, String message) {
        messageDispatcher.broadcast(path, message);
    }

    @Override
    public void handleBroadcastLocalMessage(String path, String message) {
        messageDispatcher.broadcastLocal(path, message, session -> true);
    }

    @Override
    public void handlePrivateMessage(String path, String targetSessionId, String message, boolean local) {
        if (local) {
            messageDispatcher.sendToSessionLocal(path, targetSessionId, message);
            return;
        }
        messageDispatcher.sendToSession(path, targetSessionId, message);
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