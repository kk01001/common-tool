package io.github.kk01001.push.channel.jiguang.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.push.channel.jiguang.request.PushSendParam;
import io.github.kk01001.push.channel.jiguang.request.audience.Audience;
import io.github.kk01001.push.channel.jiguang.request.message.notification.NotificationMessage;
import io.github.kk01001.push.core.MessageNotification;
import io.github.kk01001.push.core.NotificationProperties;
import io.github.kk01001.push.core.NotificationType;
import io.github.kk01001.push.core.SimpleNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2024-12-26 10:10:00
 * @description
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.channel.jiguang", name = "enabled", havingValue = "true")
public class JiGuangMessageNotificationImpl implements MessageNotification {

    private final NotificationProperties notificationProperties;

    private final ObjectMapper objectMapper;

    @Override
    public boolean support(NotificationType notificationType) {
        return NotificationType.JIGUANG.equals(notificationType);
    }

    @Override
    public <T> boolean unicast(String deviceToken, T requestData) {
        String url = notificationProperties.getJiguang().getUrl();
        try {
            String body = objectMapper.writeValueAsString(requestData);
            String appKey = notificationProperties.getJiguang().getAppKey();
            String appMasterSecret = notificationProperties.getJiguang().getAppMasterSecret();
            try (HttpResponse httpResponse = HttpRequest.post(url)
                    .body(body)
                    .basicAuth(appKey, appMasterSecret)
                    .timeout(notificationProperties.getJiguang().getTimeout())
                    .execute()) {
                if (httpResponse.isOk()) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("[U-Meng] request fail: ", e);
        }
        return false;
    }

    @Override
    public boolean simpleUnicast(SimpleNotificationRequest request) {
        String deviceToken = request.getDeviceToken();
        String title = request.getTitle();
        String message = request.getMessage();
        PushSendParam param = buildSimpleRequest(deviceToken, title, message);
        return unicast(deviceToken, param);
    }

    private PushSendParam buildSimpleRequest(String deviceToken, String title, String message) {
        PushSendParam param = new PushSendParam();
        param.setPlatform("all");

        Audience audience = new Audience();
        audience.setRegistrationIdList(List.of(deviceToken));
        param.setAudience(audience);

        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setAlert(message);

        NotificationMessage.Android android = new NotificationMessage.Android();
        android.setAlert(message);
        android.setTitle(title);
        android.setBuilderId(1);
        android.setDisplayForeground("1");
        notificationMessage.setAndroid(android);
        param.setNotification(notificationMessage);
        return param;
    }
}
