package io.github.archer099.netty.event;

import io.github.archer099.netty.cluster.WebSocketClusterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

/**
 * @author archer099
 * @date 2026-03-07 10:00:00
 * @description 消息事件监听器，将消息广播到集群（由 AutoConfiguration 注册，不使用 @Component）
 */
@Slf4j
public class WebSocketMessageEventListener implements ApplicationListener<WebSocketMessageEvent> {

    private final WebSocketClusterManager clusterManager;

    public WebSocketMessageEventListener(WebSocketClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public void onApplicationEvent(WebSocketMessageEvent event) {
        clusterManager.broadcast(event.getMessage(), event.getTargetSessionId());
    }
}
