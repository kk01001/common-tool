package io.github.kk01001.push.channel.jiguang.request.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kk01001.push.channel.jiguang.request.message.custom.CustomMessage;
import io.github.kk01001.push.channel.jiguang.request.message.notification.NotificationMessage;
import io.github.kk01001.push.channel.jiguang.request.message.sms.SmsMessage;
import lombok.Data;

@Data
public class BatchPushParam {

    /**
     * 此处填写的是 regId 值或者 alias 值
     */
    @JsonProperty("target")
    private String target;

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

    @JsonProperty("sms_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SmsMessage smsMessage;

    @JsonProperty("callback")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Callback callback;

}
