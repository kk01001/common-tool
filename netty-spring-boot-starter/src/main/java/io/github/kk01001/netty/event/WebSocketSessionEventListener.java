package io.github.kk01001.netty.event;

import io.github.kk01001.netty.cluster.WebSocketClusterManager;
import io.github.kk01001.netty.session.WebSocketSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketSessionEventListener implements ApplicationListener<WebSocketSessionEvent> {

    private final WebSocketClusterManager clusterManager;

    public WebSocketSessionEventListener(WebSocketClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public void onApplicationEvent(WebSocketSessionEvent event) {
        WebSocketSession session = event.getSession();
        switch (event.getEventType()) {
            case ADD:
                clusterManager.addSession(session);
                break;
            case REMOVE:
                clusterManager.removeSession(session.getId());
                break;
            default:
                log.warn("未知的会话事件类型: {}", event.getEventType());
        }
    }
}