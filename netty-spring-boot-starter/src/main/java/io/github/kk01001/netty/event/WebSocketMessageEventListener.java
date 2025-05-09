package io.github.kk01001.netty.event;

import io.github.kk01001.netty.cluster.WebSocketClusterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketMessageEventListener implements ApplicationListener<WebSocketMessageEvent> {

    private final WebSocketClusterManager clusterManager;

    public WebSocketMessageEventListener(WebSocketClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public void onApplicationEvent(WebSocketMessageEvent event) {
        // 收到消息 广播给每个节点
        String targetSessionId = event.getTargetSessionId();
        clusterManager.broadcast(event.getMessage(), targetSessionId);
    }
} 