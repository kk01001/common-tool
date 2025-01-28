package io.github.kk01001.robot.service;

import io.github.kk01001.robot.client.RobotClient;
import io.github.kk01001.robot.message.RobotMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 机器人服务
 * 负责管理不同类型机器人的消息发送
 */
@Slf4j
@Service
public class RobotService {

    /**
     * 机器人客户端映射
     * key: 机器人ID
     * value: 对应的机器人客户端实现
     */
    private final Map<String, RobotClient> robotClients;
    
    public RobotService(Map<String, RobotClient> robotClients) {
        this.robotClients = robotClients;
    }
    
    /**
     * 发送消息
     * @param robotId 机器人ID
     * @param message 要发送的消息
     */
    public void sendMessage(String robotId, RobotMessage message) {
        RobotClient client = robotClients.get(robotId);
        if (client == null) {
            throw new IllegalArgumentException("Robot not found: " + robotId);
        }
        client.sendMessage(message);
    }
} 