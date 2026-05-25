package io.github.archer099.mqtt.examples.service.impl;

import cn.hutool.core.util.StrUtil;
import io.github.archer099.exception.BizException;
import io.github.archer099.mqtt.core.MqttTemplate;
import io.github.archer099.mqtt.examples.dto.DanmakuSendRequestDTO;
import io.github.archer099.mqtt.examples.service.DanmakuService;
import io.github.archer099.mqtt.examples.vo.DanmakuSendResponseVO;
import io.github.archer099.util.JacksonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DanmakuServiceImpl implements DanmakuService {

    private final MqttTemplate mqttTemplate;

    @Override
    public DanmakuSendResponseVO send(DanmakuSendRequestDTO request) {
        if (Objects.isNull(request) || StrUtil.isBlank(request.getVideoId())) {
            throw new BizException("videoId不能为空");
        }
        if (StrUtil.isBlank(request.getText())) {
            throw new BizException("弹幕内容不能为空");
        }

        String topic = "video/" + request.getVideoId() + "/danmaku";

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("videoId", request.getVideoId());
        payloadMap.put("text", request.getText());
        payloadMap.put("userId", request.getUserId());
        payloadMap.put("color", request.getColor());
        payloadMap.put("fontSize", request.getFontSize());
        payloadMap.put("clientTs", System.currentTimeMillis());

        String payload = JacksonUtil.toJson(payloadMap);

        try {
            int qos = 1;
            mqttTemplate.send(topic, payload, qos);
            DanmakuSendResponseVO vo = new DanmakuSendResponseVO();
            vo.setTopic(topic);
            vo.setQos(qos);
            vo.setPayload(payload);
            return vo;
        } catch (MqttException e) {
            log.error("发送弹幕失败, topic={}", topic, e);
            throw new BizException("发送弹幕失败");
        }
    }
}