package io.github.kk01001.oss.listener;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import lombok.RequiredArgsConstructor;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 进度监听适配器
 */
@RequiredArgsConstructor
public class ProgressListenerAdapter implements ProgressListener {
    private final CustomProgressListener listener;
    private final long totalBytes;
    private long bytesRead;
    private long lastBytesRead;
    private long lastUpdateTime;

    @Override
    public void progressChanged(ProgressEvent progressEvent) {
        switch (progressEvent.getEventType()) {
            case REQUEST_BYTE_TRANSFER_EVENT:
                break;
            case RESPONSE_BYTE_TRANSFER_EVENT:
                responseByte(progressEvent);
                break;
            default:
                break;
        }
    }

    private void responseByte(ProgressEvent progressEvent) {
        bytesRead += progressEvent.getBytes();
        long currentTime = System.currentTimeMillis();

        // 计算下载速度（每秒更新一次）
        if (currentTime - lastUpdateTime >= 1000) {
            long bytesDelta = bytesRead - lastBytesRead;
            double speed;
            String speedUnit;

            // 计算速度和单位
            if (bytesDelta >= 1024 * 1024) { // 大于等于1MB/s
                speed = bytesDelta / (1024.0 * 1024.0);
                speedUnit = "MB/s";
            } else { // 小于1MB/s，使用KB/s
                speed = bytesDelta / 1024.0;
                speedUnit = "KB/s";
            }

            if (listener != null) {
                double percentage = totalBytes > 0 ? (double) bytesRead / totalBytes * 100 : 0;
                // 保留两位小数
                listener.onProgress(bytesRead, totalBytes,
                        Math.round(percentage * 100.0) / 100.0,
                        Math.round(speed * 100.0) / 100.0,
                        speedUnit);
            }

            // 更新上次的字节数和时间
            lastBytesRead = bytesRead;
            lastUpdateTime = currentTime;
        }
    }
} 