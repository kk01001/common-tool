package io.github.kk01001.push.channel.fcm.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2024-12-25 15:51:00
 * @description
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class FcmNotificationRequest {

    private Message message;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {

        @JsonProperty("data")
        private Map<String, String> data;

        @JsonProperty("notification")
        private Notification notification;

        @JsonProperty("android")
        private AndroidConfig androidConfig;

        @JsonProperty("webpush")
        private WebpushConfig webpushConfig;

        @JsonProperty("apns")
        private ApnsConfig apnsConfig;

        @JsonProperty("token")
        private String token;

        @JsonProperty("topic")
        private String topic;

        @JsonProperty("condition")
        private String condition;

        @JsonProperty("fcm_options")
        private FcmOptions fcmOptions;
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LightSettings {

        @JsonProperty("color")
        private LightSettingsColor color;

        @JsonProperty("light_on_duration")
        private String lightOnDuration;

        @JsonProperty("light_off_duration")
        private String lightOffDuration;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class LightSettingsColor {

            @JsonProperty("red")
            private Float red;

            @JsonProperty("green")
            private Float green;

            @JsonProperty("blue")
            private Float blue;

            @JsonProperty("alpha")
            private Float alpha;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FcmOptions {

        @JsonProperty("analytics_label")
        private String analyticsLabel;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApnsConfig {

        @JsonProperty("headers")
        private Map<String, String> headers;

        @JsonProperty("payload")
        private Map<String, Object> payload;

        @JsonProperty("fcm_options")
        private ApnsFcmOptions fcmOptions;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ApnsFcmOptions {

            @JsonProperty("analytics_label")
            private String analyticsLabel;

            @JsonProperty("image")
            private String image;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WebpushConfig {

        @JsonProperty("headers")
        private Map<String, String> headers;

        @JsonProperty("data")
        private Map<String, String> data;

        @JsonProperty("notification")
        private Map<String, Object> notification;

        @JsonProperty("fcm_options")
        private WebpushFcmOptions fcmOptions;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class WebpushFcmOptions {

            @JsonProperty("link")
            private String link;
        }

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Notification {

        @JsonProperty("title")
        private String title;

        @JsonProperty("body")
        private String body;

        @JsonProperty("image")
        private String image;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AndroidConfig {

        @JsonProperty("collapse_JsonProperty")
        private String collapseJsonProperty;

        @JsonProperty("priority")
        private String priority;

        @JsonProperty("ttl")
        private String ttl;

        /**
         * 用于确保推送通知只能发送到指定的 Android 应用包名。此参数增强了安全性，避免错误或恶意推送发送到非预期的应用。
         */
        @JsonProperty("restricted_package_name")
        private String restrictedPackageName;

        @JsonProperty("data")
        private Map<String, Object> data;

        @JsonProperty("notification")
        private AndroidNotification notification;

        @JsonProperty("fcm_options")
        private AndroidFcmOptions fcmOptions;

        @JsonProperty("direct_boot_ok")
        private Boolean directBootOk;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class AndroidFcmOptions {

            @JsonProperty("analytics_label")
            private String analyticsLabel;
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class AndroidNotification {

            @JsonProperty("title")
            private String title;

            @JsonProperty("body")
            private String body;

            @JsonProperty("icon")
            private String icon;

            @JsonProperty("color")
            private String color;

            @JsonProperty("sound")
            private String sound;

            @JsonProperty("tag")
            private String tag;

            @JsonProperty("click_action")
            private String clickAction;

            @JsonProperty("body_loc_JsonProperty")
            private String bodyLocJsonProperty;

            @JsonProperty("body_loc_args")
            private List<String> bodyLocArgs;

            @JsonProperty("title_loc_JsonProperty")
            private String titleLocJsonProperty;

            @JsonProperty("title_loc_args")
            private List<String> titleLocArgs;

            @JsonProperty("channel_id")
            private String channelId;

            @JsonProperty("image")
            private String image;

            @JsonProperty("ticker")
            private String ticker;

            @JsonProperty("sticky")
            private Boolean sticky;

            @JsonProperty("event_time")
            private String eventTime;

            /**
             * 主要用于控制通知是否只在本地显示，而不会同步到其他设备。这对于多设备登录的用户来说非常有用，避免同一通知在所有设备上弹出，从而优化用户体验。
             * 使用场景
             * 即时通讯应用：用户收到新消息时，让 ticker 在状态栏短暂显示“新消息到达”。
             * 警报通知：在天气、金融或交通应用中推送紧急信息时，提示用户注意新警报。
             * 新闻推送：当有重要新闻事件发生时，ticker 可以让用户及时察觉。
             */
            @JsonProperty("local_only")
            private Boolean localOnly;

            /**
             * notification_priority 用于控制 通知的显示优先级，决定通知在系统通知栏中的显示位置和提醒方式。
             * 它可以影响通知的紧急程度、是否显示在状态栏、锁屏时的展示方式，以及是否触发声音、振动等效果。
             * PRIORITY_MAX	最高优先级，用于需要立即处理的通知，例如警报、紧急消息。会触发声音、振动，并高亮显示。
             * PRIORITY_HIGH	高优先级，用于重要通知（如聊天信息）。会触发声音和振动，在状态栏靠前显示。
             * PRIORITY_DEFAULT	默认优先级，用于一般通知。不会特别突出显示，默认有声音但可能没有振动。
             * PRIORITY_LOW	低优先级，不会触发声音或振动。通知会显示在通知栏的底部。
             * PRIORITY_MIN	最低优先级，不会触发任何提醒或声音。通知仅显示在通知栏列表中。
             */
            @JsonProperty("notification_priority")
            private String priority;

            @JsonProperty("vibrate_timings")
            private List<String> vibrateTimings;

            @JsonProperty("default_vibrate_timings")
            private Boolean defaultVibrateTimings;

            @JsonProperty("default_sound")
            private Boolean defaultSound;

            @JsonProperty("light_settings")
            private LightSettings lightSettings;

            @JsonProperty("default_light_settings")
            private Boolean defaultLightSettings;

            /**
             * 当应用推送通知时，用户手机可能处于锁屏状态。visibility 参数决定通知内容在锁屏界面上显示的程度。
             * public	完整显示通知内容，即使设备处于锁屏状态。适合不包含敏感信息的通知。
             * private	隐藏通知的敏感内容，只显示应用名和“你有新消息”等提示。常用于敏感信息。
             * secret	完全隐藏通知，不在锁屏界面显示任何内容，只有解锁后才能查看。适合高度机密信息。
             */
            @JsonProperty("visibility")
            private String visibility;

            @JsonProperty("notification_count")
            private Integer notificationCount;
        }
    }
}
