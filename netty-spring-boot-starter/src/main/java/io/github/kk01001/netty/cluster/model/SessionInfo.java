package io.github.kk01001.netty.cluster.model;

import lombok.Data;

@Data
public class SessionInfo {
    private final String sessionId;
    private final String path;
    private final String nodeId;
    private final long createTime = System.currentTimeMillis();

    public SessionInfo(String sessionId, String path, String nodeId) {
        this.sessionId = sessionId;
        this.path = path;
        this.nodeId = nodeId;
    }
} 