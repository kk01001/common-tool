package io.github.kk01001.robot.client;

import io.github.kk01001.robot.message.RobotMessage;

/**
 * 机器人客户端接口
 * 定义了消息发送的基本行为
 */
public interface RobotClient {
    
    /**
     * 发送消息
     * @param message 要发送的消息对象
     */
    void sendMessage(RobotMessage message);
} 