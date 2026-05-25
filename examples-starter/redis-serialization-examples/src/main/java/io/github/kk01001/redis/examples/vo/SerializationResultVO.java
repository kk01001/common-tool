package io.github.archer099.redis.examples.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Serialization result VO
 * 
 * @author archer099
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerializationResultVO {
    
    private String codecType;
    private long writeTimeMs;
    private long readTimeMs;
    private long deleteTimeMs;
    private long totalTimeMs;
    private int iterations;
    private long maxTimeMs;
    private long minTimeMs;
    private long avgTimeMs;
    private boolean success;
    private String errorMessage;
}
