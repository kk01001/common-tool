package io.github.kk01001.push.channel.umeng.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.push.channel.umeng.request.UmengNotificationRequest;
import io.github.kk01001.push.core.MessageNotification;
import io.github.kk01001.push.core.NotificationProperties;
import io.github.kk01001.push.core.NotificationType;
import io.github.kk01001.push.core.SimpleNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * @date 2024-12-25 15:17:00
 * @description
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.channel.umeng", name = "enabled", havingValue = "true")
public class UmengMessageNotificationImpl implements MessageNotification {

    private final NotificationProperties notificationProperties;

    private final ObjectMapper objectMapper;

    @Override
    public boolean support(NotificationType notificationType) {
        return NotificationType.UMENG.equals(notificationType);
    }

    @Override
    public <T> boolean unicast(String deviceToken, T requestData) {
        String url = notificationProperties.getUmneg().getUrl();
        try {
            String body = objectMapper.writeValueAsString(requestData);
            String sign = createSign(body);
            try (HttpResponse httpResponse = HttpRequest.post(url + "?sign=" + sign)
                    .body(body)
                    .timeout(notificationProperties.getUmneg().getTimeout())
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
        UmengNotificationRequest simpleRequest = buildSimpleRequest(deviceToken, title, message);
        return unicast(null, simpleRequest);
    }

    public UmengNotificationRequest buildSimpleRequest(String deviceToken, String title, String message) {

        // 创建 NotificationRequest 对象
        UmengNotificationRequest request = new UmengNotificationRequest();

        // 设置 appkey
        request.setAppKey(notificationProperties.getUmneg().getAppKey());

        // 设置 timestamp
        request.setTimestamp(System.currentTimeMillis());

        // 设置 type
        request.setType("unicast");

        // 设置 production_mode
        request.setProductionMode("true");

        // 设置 device_tokens
        request.setDeviceTokens(deviceToken);

        // 设置 category
        request.setCategory(1);

        // 设置 payload
        UmengNotificationRequest.Payload payload = new UmengNotificationRequest.Payload();
        payload.setDisplayType("notification");

        UmengNotificationRequest.Body body = new UmengNotificationRequest.Body();
        body.setTitle(title);
        body.setText(message);
        body.setAfterOpen("go_app");

        payload.setBody(body);
        request.setPayload(payload);

        // 设置 policy
        UmengNotificationRequest.Policy policy = new UmengNotificationRequest.Policy();
        request.setPolicy(policy);

        // 设置 channel_properties
        UmengNotificationRequest.ChannelProperties channelProperties = new UmengNotificationRequest.ChannelProperties();
        channelProperties.setHuaweiChannelImportance("NORMAL");
        request.setChannelProperties(channelProperties);

        // 设置 description
        request.setDescription("Android");
        return request;
    }

    private String createSign(String body) {
        String url = notificationProperties.getUmneg().getUrl();
        String appMasterSecret = notificationProperties.getUmneg().getAppMasterSecret();
        return SecureUtil.md5(String.format("%s%s%s%s", "POST", url, body, appMasterSecret));
    }
}
