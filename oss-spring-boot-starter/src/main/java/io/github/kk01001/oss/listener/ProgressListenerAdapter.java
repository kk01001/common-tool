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

    private final ByteProgressListener listener;
    private final long totalBytes;
    private long bytesRead;

    @Override
    public void progressChanged(ProgressEvent progressEvent) {
        switch (progressEvent.getEventType()) {
            case RESPONSE_BYTE_TRANSFER_EVENT:
                bytesRead += progressEvent.getBytes();
                if (listener != null) {
                    double percentage = totalBytes > 0 ? (double) bytesRead / totalBytes * 100 : 0;
                    listener.onProgress(bytesRead, totalBytes, percentage);
                }
                break;
            default:
                break;
        }
    }
} 