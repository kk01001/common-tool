package io.github.kk01001.push.channel.fcm.service;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.push.channel.fcm.request.FcmNotificationRequest;
import io.github.kk01001.push.channel.fcm.request.GoogleAuthTokenDTO;
import io.github.kk01001.push.core.MessageNotification;
import io.github.kk01001.push.core.NotificationProperties;
import io.github.kk01001.push.core.NotificationType;
import io.github.kk01001.push.core.SimpleNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2024-12-25 15:54:00
 * @description
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.channel.fcm", name = "enabled", havingValue = "true")
public class FcmMessageNotificationImpl implements MessageNotification {

    private final NotificationProperties notificationProperties;

    private final ObjectMapper objectMapper;

    @Override
    public boolean support(NotificationType notificationType) {
        return NotificationType.FCM.equals(notificationType);
    }

    @Override
    public <T> boolean unicast(String deviceToken, T requestData) {
        GoogleAuthTokenDTO authToken = getAuthToken();
        NotificationProperties.Fcm fcm = notificationProperties.getFcm();
        try {
            String json = objectMapper.writeValueAsString(requestData);
            String googleFcmSendUrl = fcm.getGoogleFcmSendUrl();
            String projectId = fcm.getProjectId();
            String url = StrFormatter.format(googleFcmSendUrl, projectId);
            try (HttpResponse httpResponse = HttpRequest.post(url)
                    .header("Authorization", authToken.getTokenType() + " " + authToken.getAccessToken())
                    .body(json)
                    .timeout(10000)
                    .execute()) {
                if (httpResponse.isOk()) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("[FCM] request fail: ", e);
        }
        return false;
    }

    @SneakyThrows
    @Override
    public boolean simpleUnicast(SimpleNotificationRequest request) {
        FcmNotificationRequest simpleRequest = buildSimpleRequest(request);
        return unicast(null, simpleRequest);
    }

    @SneakyThrows
    private GoogleAuthTokenDTO getAuthToken() {
        NotificationProperties.Fcm fcm = notificationProperties.getFcm();
        String privateKey = fcm.getPrivateKey();
        String clientEmail = fcm.getClientEmail();
        String jwt = createJwt(privateKey, clientEmail);

        /**
         * {
         * 	"access_token": "ya29.c.c0ASRK0GbSnvazwhz079ugyLfIFSrXzYioh5rOqFdOEzbhtDHnjOfpmBuYSw0NQYjdXSVmvWR66XHtOYw6qJ-Ps6t9t5dOUFBnQv0TpMbqFRSRDjGuBsRqBq2ZPlsi1URNYbTYiEZz_K-ZmRj7Y-jYbXjFUsF2gIONvuSXFy_xnMOrkCLVB2XXH7BpyLoN7V9rRfOlCF6sGq6TOYYYNo87sYu4_-Wh42v9KRq5t5082hfSwhNVh6SIBhU-a_cGlG6QyNIQuSRGNitIfWma0ygP6KYH2V5R-lglh15OIO1MK2lJ6lsABWxX-d22yeh6wcG38EM9WjVqV9tm4HR5FFiTqyjlA1L6XScwYvjl9O0dTaosWeGP2oPRAsCLL385COROeOvtrxeo0m1SbyScUn9r-6zmWR3O0QnQUUml0Yr3_9jgjrOv2dQps0sFze6UdrBZklQWl-vggiaprpIFMJlf_lWci2latxwwF58Sh8opjm1ts8mjJIo5uocJ3MW-2o_xnMryd_8302yn9-RZr3zl7wleFssbfkhF3uckat5pbvOFsZJw9pV0utkFQRdJ_gfpVJtXyMck1Yu7F-rIIfb1c_RJg-oqYk99zWW402JeY6sq4sqOZU_RBOxwrWY4o7fWe8F6uOv1epvBsor8vZXxyMUhYtdjrY8uRnOBa3qwv_eoXI5Sirl67r1QRyvzcOto69oqcfwxs-4Wb5Z2Zwszu-3zv1an5Rycp5sXeY2zsx1SUMiqQgwardqZQYlbgwVf6bdrWqJ_1yXk2g8eBO1Q100ZfgJFxSOVdd9gBxm3fW_n2bexSqj9IksQm8a_1QOcte28pmJMsrBmvc27jpRlS2m-sz8d_MdJhfycbSmp1itpqk9ywfornvdmYqoMYayX83SzJZFgBOthdvnZkrozeRxd1zrSOg9QWu8W0lMMysSv--RMqShgMlzZzI7cB4v-a1eo3hVqBM-BmIJY-RyXj7XSOuq-RFU55x3f8FhuInVR3pW6W-M1qpv",
         * 	"expires_in": 3599,
         * 	"token_type": "Bearer"
         * }
         */
        String url = StrFormatter.format(fcm.getGoogleGetTokenUrl(), jwt);
        String post = HttpUtil.post(url, "");
        log.info("google token: {}", post);
        return objectMapper.readValue(post, GoogleAuthTokenDTO.class);
    }

    // 读取服务账户的私钥文件（PKCS8 编码）
    private RSAPrivateKey getPrivateKey(String privateKeyContent) throws Exception {
        privateKeyContent = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    // 生成 JWT
    private String createJwt(String privateKey, String clientEmail) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(null, getPrivateKey(privateKey));

        long currentTimeMillis = System.currentTimeMillis();
        Date now = new Date(currentTimeMillis);
        Date expireTime = new Date(currentTimeMillis + 3600 * 1000); // 1小时过期

        return JWT.create()
                .withIssuer(clientEmail)  // 服务账户的 client_email
                .withAudience("https://oauth2.googleapis.com/token")  // Google OAuth 2.0 令牌端点
                .withClaim("scope", "https://www.googleapis.com/auth/firebase.messaging")  // 权限范围
                .withIssuedAt(now)
                .withExpiresAt(expireTime)
                .sign(algorithm);  // 使用私钥签名
    }


    protected FcmNotificationRequest buildSimpleRequest(SimpleNotificationRequest request) throws JsonProcessingException {
        Map<String, Object> dataMap = new HashMap<>(2);
        dataMap.put("notice_data", objectMapper.writeValueAsString(request.getExtra()));
        return FcmNotificationRequest.builder()
                .message(FcmNotificationRequest.Message.builder()
                        .token(request.getDeviceToken())
                        .androidConfig(FcmNotificationRequest.AndroidConfig.builder()
                                .ttl("3600s")
                                .priority("high")
                                .data(dataMap)
                                .notification(FcmNotificationRequest.AndroidConfig.AndroidNotification.builder()
                                        .title(request.getTitle())
                                        .body(request.getMessage())
                                        .build())
                                .build())
                        .build()
                )
                .build();
    }
}
