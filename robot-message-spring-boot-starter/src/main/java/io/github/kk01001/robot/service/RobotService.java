package io.github.kk01001.robot.service;

import io.github.kk01001.robot.client.RobotClient;
import io.github.kk01001.robot.config.RobotProperties;
import io.github.kk01001.robot.message.RobotMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 机器人服务
 * 负责管理不同类型机器人的消息发送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RobotService {

    /**
     * 机器人配置属性
     */
    private final RobotProperties robotProperties;

    /**
     * 发送消息
     * @param robotId 机器人ID
     * @param message 要发送的消息
     */
    public void sendMessage(String robotId, RobotMessage message) {
        RobotClient client = robotProperties.getRobotClients().get(robotId);
        if (client == null) {
            throw new IllegalArgumentException("Robot not found: " + robotId);
        }
        client.sendMessage(message);
    }
} 