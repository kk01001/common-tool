package io.github.kk01001.robot.client;

import io.github.kk01001.robot.message.RobotMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 钉钉机器人客户端
 * 实现钉钉自定义机器人的消息发送功能
 */
@Slf4j
public class DingTalkRobotClient implements RobotClient {

    /**
     * 钉钉机器人webhook地址
     */
    private final String webhook;
    
    /**
     * 钉钉机器人安全设置的签名密钥
     */
    private final String secret;
    
    /**
     * HTTP请求客户端
     */
    private final RestTemplate restTemplate;
    
    public DingTalkRobotClient(String webhook, String secret, RestTemplate restTemplate) {
        this.webhook = webhook;
        this.secret = secret;
        this.restTemplate = restTemplate;
    }

    @Override
    public String getRobotType() {
        return "dingtalk";
    }

    @Override
    public void sendMessage(RobotMessage message) {
        String url = generateSignedUrl();
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                    message.toMessageMap(getRobotType()),
                String.class
            );
            log.info("DingTalk robot response: {}", response.getBody());
        } catch (Exception e) {
            log.error("Failed to send message to DingTalk", e);
            throw new RuntimeException("Failed to send message to DingTalk", e);
        }
    }
    
    /**
     * 生成带签名的URL
     * 钉钉机器人要求对每个请求进行签名
     */
    private String generateSignedUrl() {
        try {
            long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + secret;
            
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);
            
            return webhook + "&timestamp=" + timestamp + "&sign=" + sign;
        } catch (Exception e) {
            log.error("Failed to generate signed url", e);
            return webhook;
        }
    }
} 