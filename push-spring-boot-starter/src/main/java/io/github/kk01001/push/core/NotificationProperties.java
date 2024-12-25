package io.github.kk01001.push.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @date 2024-12-25 15:19:00
 * @description
 */
@Data
@Component
@ConfigurationProperties(prefix = "notification.channel")
public class NotificationProperties {

    private Umneg umneg = new Umneg();

    private Fcm fcm = new Fcm();

    private APNS apns = new APNS();

    @Data
    public static class Umneg {

        private Boolean enabled = true;

        private String url = "https://msgapi.umeng.com/api/send";

        private String appKey;

        private String appMasterSecret;

        private Integer timeout = 3000;
    }

    @Data
    public static class Fcm {

        private Boolean enabled = true;

        private String googleGetTokenUrl = "https://180.255.66.106:10444/googleapis/aouth2/token?grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion={}";

        private String googleFcmSendUrl = "https://180.255.66.106:10444/v1/projects/{}/messages:send";

        private String projectId;

        private String clientEmail;

        private String privateKey;
    }

    @Data
    public static class APNS {

        private Boolean enabled = true;

        private String notificationUrl = "https://api.sandbox.push.apple.com:443/3/device/{}";

        private String teamId = "E5KJ8HF8M5";

        private String keyId = "QRBHKWT65B";

        private String boundId = "com.aicommunicationassistant.linkcircle";

        private String privateKey = "-----BEGIN PRIVATE KEY-----\n" +
                "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgkw1TztH7FfHhIYRD\n" +
                "wC8EA7q/Q9Jj2k5CNHYgUtgiazmgCgYIKoZIzj0DAQehRANCAAQzlT6fQSvCWexF\n" +
                "JjYoZeGQaMnIesBFEfJt73iMBgj9dbCF7exu3AQGLy/avhTieN8dnuyhZpkd8DfT\n" +
                "3P33SEmi\n" +
                "-----END PRIVATE KEY-----";

        private String soundUrl;
    }
}
