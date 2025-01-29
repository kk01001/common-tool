package io.github.kk01001.robot.client;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import io.github.kk01001.robot.message.RobotMessage;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 钉钉机器人客户端
 * 实现钉钉群机器人的消息发送功能
 */
@Slf4j
public class DingTalkRobotClient implements RobotClient {

    /**
     * 钉钉机器人webhook地址
     */
    private final String webhook;

    /**
     * 安全设置的签名密钥
     */
    private final String secret;

    public DingTalkRobotClient(String webhook, String secret) {
        this.webhook = webhook;
        this.secret = secret;
    }

    @Override
    public String getRobotType() {
        return "dingtalk";
    }

    @Override
    public void sendMessage(RobotMessage message) {
        try {
            // 生成签名
            String timestamp = String.valueOf(System.currentTimeMillis());
            String stringToSign = timestamp + "\n" + secret;

            HMac hMac = SecureUtil.hmacSha256(secret.getBytes(StandardCharsets.UTF_8));
            byte[] signData = hMac.digest(stringToSign);
            String sign = URLEncoder.encode(Base64.encode(signData), StandardCharsets.UTF_8);
            
            // 构建请求URL
            String url = webhook + "&timestamp=" + timestamp + "&sign=" + sign;

            // 发送请求
            String response = HttpUtil.post(
                url,
                    JSONUtil.toJsonStr(message.toMessageMap(getRobotType()))
            );
            log.info("DingTalk robot response: {}", response);

            if (response != null && response.contains("\"errcode\":0")) {
                log.debug("Message sent successfully");
            } else {
                log.error("Failed to send message, response: {}", response);
                throw new RuntimeException("Failed to send message: " + response);
            }
        } catch (Exception e) {
            log.error("Failed to send message to DingTalk", e);
            throw new RuntimeException("Failed to send message to DingTalk", e);
        }
    }
} 