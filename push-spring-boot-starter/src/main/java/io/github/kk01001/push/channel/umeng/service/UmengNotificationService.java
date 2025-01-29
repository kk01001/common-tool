package io.github.kk01001.push.channel.umeng.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kk01001.push.channel.umeng.request.UmengNotificationRequest;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * @date 2024-12-25 14:47:00
 * @description
 */
@Service
public class UmengNotificationService {

    public static void main(String[] args) throws JsonProcessingException {
        String url = "https://msgapi.umeng.com/api/send";
        String appKye = "676b9aa47e5e6a4eebc5dbca";
        String deviceToken = "99999";
        String messageSecret = "06133080cc727caf18d9d6fc9b8058c9";
        String masterSecret = "zrw9evvxau5bhkoxdsx0kexgaopbtv37";

        UmengNotificationRequest notificationRequest = build(appKye, deviceToken);
        String json = new ObjectMapper().writeValueAsString(notificationRequest);
        System.out.println(json);
        String sign = SecureUtil.md5(String.format("%s%s%s%s", "POST", url, json, masterSecret));
        System.out.println(sign);
        HttpResponse httpResponse = HttpRequest.post(url + "?sign=" + sign)
                .body(json)
                .execute();
        System.out.println(httpResponse);
    }

    public static UmengNotificationRequest build(String appKey, String deviceToken) {

        // 创建 NotificationRequest 对象
        UmengNotificationRequest request = new UmengNotificationRequest();

        // 设置 appkey
        request.setAppKey(appKey);

        // 设置 timestamp
        request.setTimestamp(System.currentTimeMillis());

        // 设置 type
        request.setType("unicast");

        // 设置 production_mode
        request.setProductionMode("false");

        // 设置 device_tokens
        request.setDeviceTokens(deviceToken);

        // 设置 category
        request.setCategory(1);

        // 设置 payload
        UmengNotificationRequest.Payload payload = new UmengNotificationRequest.Payload();
        payload.setDisplayType("notification");

        UmengNotificationRequest.Body body = new UmengNotificationRequest.Body();
        body.setTitle("您有一个来电");
        body.setText("您有一个来电");
        body.setAfterOpen("go_app");

        payload.setBody(body);
        request.setPayload(payload);

        // 设置 policy
        UmengNotificationRequest.Policy policy = new UmengNotificationRequest.Policy();
        policy.setExpireTime("2025-07-20 12:00:00");
        request.setPolicy(policy);

        // 设置 channel_properties
        UmengNotificationRequest.ChannelProperties channelProperties = new UmengNotificationRequest.ChannelProperties();
        // channelProperties.setChannelActivity("xxx");
        channelProperties.setHuaweiChannelImportance("NORMAL");
        request.setChannelProperties(channelProperties);

        // 设置 description
        request.setDescription("测试单播消息-Android");
        return request;
    }
}
