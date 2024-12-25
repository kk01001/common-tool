package io.github.kk01001.push.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2024-12-25 15:42:00
 * @description
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageNotificationFactory {

    private final List<MessageNotification> messageNotifications;

    public <T> boolean unicast(NotificationType type, String deviceToken, T requestData) {
        Optional<MessageNotification> optional = messageNotifications.stream()
                .filter(messageNotification -> messageNotification.support(type))
                .findFirst();
        if (optional.isEmpty()) {
            throw new RuntimeException("unsupported notification type.");
        }

        return optional.get().unicast(deviceToken, requestData);
    }

    public boolean simpleUnicast(NotificationType type, SimpleNotificationRequest request) {
        Optional<MessageNotification> optional = messageNotifications.stream()
                .filter(messageNotification -> messageNotification.support(type))
                .findFirst();
        if (optional.isEmpty()) {
            throw new RuntimeException("unsupported notification type.");
        }
        return optional.get().simpleUnicast(request);
    }

}
