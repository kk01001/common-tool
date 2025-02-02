package io.github.kk01001.netty.cluster.model;

import lombok.Data;

@Data
public class BroadcastMessage {
    private final String path;
    private final String message;
    private final String sourceNodeId;
    private final long timestamp = System.currentTimeMillis();

    public BroadcastMessage(String path, String message, String sourceNodeId) {
        this.path = path;
        this.message = message;
        this.sourceNodeId = sourceNodeId;
    }
} 