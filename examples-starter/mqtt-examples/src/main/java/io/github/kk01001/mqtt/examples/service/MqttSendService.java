package io.github.kk01001.mqtt.examples.service;

import io.github.kk01001.mqtt.examples.dto.MqttBulkSendRequestDTO;
import io.github.kk01001.mqtt.examples.vo.MqttBulkSendResultVO;

/**
 * @author kk01001
 * @date 2025-11-17 11:08:00
 * @description MQTT 发送服务接口
 */
public interface MqttSendService {

    /**
     * 批量发送消息
     *
     * @param request 请求参数
     * @return 发送结果
     */
    MqttBulkSendResultVO bulkSend(MqttBulkSendRequestDTO request);
}