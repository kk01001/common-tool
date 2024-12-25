package io.github.kk01001.push.core;

import lombok.Data;

import java.util.Map;

/**
 * @author linshiqiang
 * @date 2024-12-25 15:13:00
 * @description
 */
@Data
public class SimpleNotificationRequest {

    private NotificationType notificationType;

    private String deviceToken;

    private String title;

    private String message;

    private Map<String, Object> extra;
}
