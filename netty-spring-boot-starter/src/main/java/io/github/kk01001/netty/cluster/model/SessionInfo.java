package io.github.kk01001.netty.cluster.model;

import io.github.kk01001.netty.session.WebSocketSession;
import lombok.Data;

@Data
public class SessionInfo {

    /**
     * 会话ID
     */
    private final String sessionId;

    /**
     * 会话路径
     */
    private final String path;

    /**
     * 节点ID
     */
    private final String nodeId;

    /**
     * 用户ID
     */
    private final String userId;

    /**
     * 远端地址
     */
    private final String remoteAddress;

    /**
     * 创建时间
     */
    private final long createTime = System.currentTimeMillis();

    // WebSocketSession -> SessionInfo
    public SessionInfo(WebSocketSession session, String nodeId) {
        this.sessionId = session.getId();
        this.path = session.getPath();
        this.nodeId = nodeId;
        this.userId = session.getUserId();
        this.remoteAddress = session.getChannel().remoteAddress().toString();
    }
} 