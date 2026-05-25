package io.github.archer099.netty.event;

import io.github.archer099.netty.cluster.WebSocketClusterManager;
import io.github.archer099.netty.session.WebSocketSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

/**
 * @author archer099
 * @date 2026-03-07 10:00:00
 * @description 会话事件监听器，将会话变更同步到集群管理器（由 AutoConfiguration 注册，不使用 @Component）
 */
@Slf4j
public class WebSocketSessionEventListener implements ApplicationListener<WebSocketSessionEvent> {

    private final WebSocketClusterManager clusterManager;

    public WebSocketSessionEventListener(WebSocketClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public void onApplicationEvent(WebSocketSessionEvent event) {
        WebSocketSession session = event.getSession();
        switch (event.getEventType()) {
            case ADD -> clusterManager.addSession(session);
            case REMOVE -> clusterManager.removeSession(session.getId());
            default -> log.warn("未知的会话事件类型: {}", event.getEventType());
        }
    }
}
