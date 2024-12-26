package io.github.kk01001.push.channel.jiguang.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kk01001.push.channel.jiguang.request.audience.Audience;
import io.github.kk01001.push.channel.jiguang.request.callback.Callback;
import io.github.kk01001.push.channel.jiguang.request.message.custom.CustomMessage;
import io.github.kk01001.push.channel.jiguang.request.message.inapp.InAppMessage;
import io.github.kk01001.push.channel.jiguang.request.message.notification.NotificationMessage;
import io.github.kk01001.push.channel.jiguang.request.message.notification.ThirdNotificationMessage;
import io.github.kk01001.push.channel.jiguang.request.message.sms.SmsMessage;
import io.github.kk01001.push.channel.jiguang.request.options.Options;
import lombok.Data;

@Data
public class PushSendParam {

    @JsonProperty("cid")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cid;

    /**
     * 两种格式
     * 字符串："all"
     * {@link Audience}对象: {"tag":[],"tag_and":[],"tag_not":[],"alias":[],"registration_id":[],"segment":[],"abtest":[],"live_activity_id":"","file":{"file_id":""}}
     */
    @JsonProperty("audience")
    private Object audience;

    /**
     * 两种格式
     * 字符串："all"
     * {@link Platform}数组：["android","ios","hmos","quickapp"]
     */
    @JsonProperty("platform")
    private Object platform;

    @JsonProperty("options")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Options options;

    @JsonProperty("notification")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private NotificationMessage notification;

    @JsonProperty("message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CustomMessage custom;

    @JsonProperty("inapp_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private InAppMessage inApp;

    @JsonProperty("notification_3rd")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ThirdNotificationMessage thirdNotificationMessage;

    @JsonProperty("sms_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SmsMessage smsMessage;

    @JsonProperty("callback")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Callback callback;

}
