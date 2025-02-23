package io.github.kk01001.oss.listener;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 下载进度监听器
 */
public interface ByteProgressListener {
    /**
     * 进度回调
     *
     * @param bytesRead  已读取的字节数
     * @param totalBytes 总字节数
     * @param percentage 进度百分比
     */
    void onProgress(long bytesRead, long totalBytes, double percentage);
} 