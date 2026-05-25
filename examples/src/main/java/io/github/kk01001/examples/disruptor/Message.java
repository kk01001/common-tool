package io.github.archer099.examples.disruptor;

import lombok.Data;

@Data
public class Message {
    private String id;
    private String content;
    private Long timestamp;
} 