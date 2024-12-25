package io.github.kk01001.push.channel.apns.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author linshiqiang
 * @date 2024-12-25 16:09:00
 * @description
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApnsNotificationRequest {

    private Aps aps;

    private Map<String, Object> payload;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Aps {

        private Alert alert;

        private Integer badge;

        private String sound;

        private String category;

        private Long timestamp;

        @JsonProperty("push-type")
        private String pushType;

        @JsonProperty("call-id")
        private String callId;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Alert {

            private String title;

            private String subtitle;

            private String body;

            @JsonProperty("launch-image")
            private String launchImage;
        }

    }

}
