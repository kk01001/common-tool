package io.github.kk01001.push.core;

/**
 * @author linshiqiang
 * @date 2024-12-25 15:12:00
 * @description
 */
public interface MessageNotification {

    boolean support(NotificationType notificationType);

    <T> boolean unicast(String deviceToken, T requestData);

    boolean simpleUnicast(SimpleNotificationRequest request);

}
