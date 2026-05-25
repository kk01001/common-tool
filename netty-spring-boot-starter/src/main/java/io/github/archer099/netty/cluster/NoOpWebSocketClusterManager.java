package io.github.archer099.netty.cluster;

import io.github.archer099.netty.session.WebSocketSession;
import lombok.extern.slf4j.Slf4j;

/**
 * @author archer099
 * @date 2026-03-07 10:00:00
 * @description 空操作的集群管理器，非集群模式下使用
 */
@Slf4j
public class NoOpWebSocketClusterManager implements WebSocketClusterManager {

    @Override
    public void addSession(WebSocketSession session) {
    }

    @Override
    public void removeSession(String sessionId) {
    }

    @Override
    public void broadcast(String message, String targetSessionId) {
    }

    @Override
    public String getNodeId() {
        return "standalone";
    }

    @Override
    public void init() {
        log.info("WebSocket running in standalone mode (cluster disabled)");
    }

    @Override
    public void destroy() {
    }
}
