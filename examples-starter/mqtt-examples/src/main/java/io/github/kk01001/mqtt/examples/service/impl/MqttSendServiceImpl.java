package io.github.archer099.mqtt.examples.service.impl;

import io.github.archer099.common.model.ApiResponse; // only for potential reference
import io.github.archer099.exception.BizException;
import io.github.archer099.mqtt.config.MqttProperties;
import io.github.archer099.mqtt.core.MqttTemplate;
import io.github.archer099.mqtt.examples.dto.MqttBulkSendRequestDTO;
import io.github.archer099.mqtt.examples.service.MqttSendService;
import io.github.archer099.mqtt.examples.vo.MqttBulkSendResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author archer099
 * @date 2025-11-17 11:08:00
 * @description MQTT 发送服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MqttSendServiceImpl implements MqttSendService {

    private final MqttTemplate mqttTemplate;
    private final MqttProperties mqttProperties;

    @Override
    public MqttBulkSendResultVO bulkSend(MqttBulkSendRequestDTO request) {
        if (Objects.isNull(mqttProperties) || !Boolean.TRUE.equals(mqttProperties.getEnabled())) {
            throw new BizException("请先开启 mqtt.enabled 并配置 brokerUrl");
        }

        String topic = request.getTopic();
        int count = request.getCount();
        int qos = Objects.isNull(request.getQos()) ? 2 : request.getQos();
        String prefix = request.getPayloadPrefix();

        long start = System.currentTimeMillis();
        int success = 0;
        int failure = 0;

        for (int i = 0; i < count; i++) {
            try {
                String payload = prefix + " " + i;
                mqttTemplate.send(topic, payload, qos);
                success++;
            } catch (MqttException e) {
                failure++;
                log.error("批量发送消息失败, topic={}, index={}", topic, i, e);
            }
        }

        long duration = System.currentTimeMillis() - start;
        MqttBulkSendResultVO vo = new MqttBulkSendResultVO();
        vo.setTopic(topic);
        vo.setCount(count);
        vo.setQos(qos);
        vo.setSuccessCount(success);
        vo.setFailureCount(failure);
        vo.setDurationMs(duration);
        return vo;
    }
}