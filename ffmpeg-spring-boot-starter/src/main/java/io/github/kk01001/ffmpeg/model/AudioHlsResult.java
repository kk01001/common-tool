package io.github.kk01001.ffmpeg.model;

import lombok.Data;

/**
 * @author kk01001
 * @date 2025-11-17 16:22:00
 * @description 音频转 HLS 结果
 */
@Data
public class AudioHlsResult {

    /** 
     * m3u8 清单相对/绝对路径 
     */
    private String m3u8Path;

    /** 分片目录 */
    private String segmentDir;

    /** 
     * 分片数量（估计值） 
     */
    private Integer segmentCount;

    /** 
     * 执行耗时毫秒 
     */
    private Long durationMs;
}