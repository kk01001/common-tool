package io.github.archer099.ffmpeg.model;

import lombok.Data;

/**
 * @author archer099
 * @date 2025-11-17 16:22:00
 * @description ffprobe 媒体信息结果
 */
@Data
public class FfprobeResult {
    
    /** 
     * 原始 JSON 字符串 
     */
    private String rawJson;
}