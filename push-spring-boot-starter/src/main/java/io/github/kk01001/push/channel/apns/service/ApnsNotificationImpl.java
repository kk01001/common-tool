package io.github.kk01001.push.channel.apns.service;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.http.ContentType;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.push.channel.apns.request.ApnsNotificationRequest;
import io.github.kk01001.push.core.MessageNotification;
import io.github.kk01001.push.core.NotificationProperties;
import io.github.kk01001.push.core.NotificationType;
import io.github.kk01001.push.core.SimpleNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2024-12-25 16:10:00
 * @description
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApnsNotificationImpl implements MessageNotification {

    private final NotificationProperties notificationProperties;

    private final ObjectMapper objectMapper;

    @Override
    public boolean support(NotificationType notificationType) {
        return NotificationType.APNS.equals(notificationType);
    }

    @SneakyThrows
    @Override
    public <T> boolean unicast(String deviceToken, T requestData) {
        String body = objectMapper.writeValueAsString(requestData);
        String url = StrFormatter.format(notificationProperties.getApns().getNotificationUrl(), deviceToken);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse(ContentType.JSON.getValue());
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body, mediaType))
                .addHeader("Authorization", "bearer " + createJwt())
                // .addHeader("apns-id", notificationDTO.getCallId())
                .addHeader("apns-push-type", "alert")
                .addHeader("apns-expiration", "0")
                .addHeader("apns-priority", "10")
                .addHeader("apns-topic", notificationProperties.getApns().getBoundId())
                .build();
        try (Response response = client.newCall(request).execute()) {
            log.info("[APNS] notice success: {}, result: {}", response.isSuccessful(), getResult(response));
            return true;
        } catch (Exception e) {
            log.error("[APNS] notice error: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public boolean simpleUnicast(SimpleNotificationRequest request) {
        ApnsNotificationRequest simpleRequest = buildSimpleRequest(request);
        return unicast(request.getDeviceToken(), simpleRequest);
    }

    private Object getResult(Response response) {
        ResponseBody body = response.body();
        try {
            if (response.isSuccessful() && Objects.nonNull(body)) {
                return new String(body.bytes());
            }
        } catch (Exception e) {
            // do nothing
        }
        return null;
    }

    private String createJwt() throws Exception {
        Algorithm algorithm = Algorithm.ECDSA256(null, getPrivateKey(notificationProperties.getApns().getPrivateKey()));
        NotificationProperties.APNS apns = notificationProperties.getApns();
        return JWT.create()
                .withKeyId(apns.getKeyId())
                .withIssuer(apns.getTeamId())
                .withIssuedAt(Instant.ofEpochSecond(System.currentTimeMillis() / 1000))
                .withClaim("typ", "")
                .sign(algorithm);
    }

    private ECPrivateKey getPrivateKey(String privateKeyContent) throws Exception {
        privateKeyContent = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return (ECPrivateKey) kf.generatePrivate(spec);
    }


    public ApnsNotificationRequest buildSimpleRequest(SimpleNotificationRequest request) {
        Map<String, Object> payloadMap = new HashMap<>(2);
        payloadMap.put("notice_data", request.getExtra());
        ApnsNotificationRequest.Aps.Alert alert = ApnsNotificationRequest.Aps.Alert.builder()
                .title(request.getTitle())
                .body(request.getMessage())
                .build();

        ApnsNotificationRequest.Aps aps = ApnsNotificationRequest.Aps.builder()
                .alert(alert)
                .build();
        return ApnsNotificationRequest.builder()
                .aps(aps)
                .payload(payloadMap)
                .build();
    }
}
