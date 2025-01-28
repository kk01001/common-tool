package io.github.kk01001.robot.client;

import io.github.kk01001.robot.message.RobotMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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

    /**
     * HTTP请求客户端
     */
    private final RestTemplate restTemplate;

    public WeChatRobotClient(String webhook, String key, RestTemplate restTemplate) {
        // webhook格式: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx
        if (!webhook.contains("key=")) {
            throw new IllegalArgumentException("WeChat webhook must contain key parameter");
        }
        this.webhook = webhook;
        this.key = key;
        this.restTemplate = restTemplate;
    }

    @Override
    public String getRobotType() {
        return "wechat";
    }

    @Override
    public void sendMessage(RobotMessage message) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                webhook,
                    message.toMessageMap(getRobotType()),
                String.class
            );
            log.info("WeChat robot response: {}", response.getBody());

            if (response.getBody() != null && response.getBody().contains("\"errcode\":0")) {
                log.debug("Message sent successfully");
            } else {
                log.error("Failed to send message, response: {}", response.getBody());
                throw new RuntimeException("Failed to send message: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to send message to WeChat", e);
            throw new RuntimeException("Failed to send message to WeChat", e);
        }
    }
} 