package io.github.kk01001.robot.client;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import io.github.kk01001.robot.message.RobotMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * 企业微信机器人客户端
 * 实现企业微信群机器人的消息发送功能
 */
@Slf4j
public class WeChatRobotClient implements RobotClient {

    /**
     * 企业微信机器人webhook地址
     */
    private final String webhook;

    /**
     * 企业微信机器人key
     */
    private final String key;

    public WeChatRobotClient(String webhook, String key) {
        // webhook格式: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx
        if (!webhook.contains("key=")) {
            throw new IllegalArgumentException("WeChat webhook must contain key parameter");
        }
        this.webhook = webhook;
        this.key = key;
    }

    @Override
    public String getRobotType() {
        return "wechat";
    }

    @Override
    public void sendMessage(RobotMessage message) {
        try {
            String response = HttpUtil.post(
                webhook,
                JSONUtil.toJsonStr(message.toMessageMap(getRobotType()))
            );
            log.info("WeChat robot response: {}", response);

            if (response != null && response.contains("\"errcode\":0")) {
                log.debug("Message sent successfully");
            } else {
                log.error("Failed to send message, response: {}", response);
                throw new RuntimeException("Failed to send message: " + response);
            }
        } catch (Exception e) {
            log.error("Failed to send message to WeChat", e);
            throw new RuntimeException("Failed to send message to WeChat", e);
        }
    }
} 