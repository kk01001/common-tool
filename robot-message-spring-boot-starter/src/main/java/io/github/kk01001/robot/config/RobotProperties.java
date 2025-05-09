package io.github.kk01001.robot.config;

import io.github.kk01001.robot.client.*;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.HashMap;
import java.util.Map;

/**
 * 机器人配置属性
 * 用于配置不同类型机器人的参数
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "robot")
public class RobotProperties implements InitializingBean {

    private final Map<String, RobotClient> robotClients = new HashMap<>();

    /**
     * 钉钉机器人配置
     * key: 机器人ID
     * value: 机器人配置信息
     */
    private Map<String, DingTalkRobotConfig> dingtalk = new HashMap<>();
    
    /**
     * 企业微信机器人配置
     * key: 机器人ID
     * value: 机器人配置信息
     */
    private Map<String, WeChatRobotConfig> wechat = new HashMap<>();
    
    /**
     * 邮件机器人配置
     * key: 机器人ID
     * value: 机器人配置信息
     */
    private Map<String, EmailRobotConfig> email = new HashMap<>();
    
    /**
     * 短信配置
     * key: 机器人ID
     * value: 短信配置信息
     */
    private Map<String, SmsRobotConfig> sms = new HashMap<>();
    
    /**
     * 钉钉机器人配置项
     */
    @Data
    public static class DingTalkRobotConfig {
        /**
         * webhook地址
         */
        private String webhook;
        /**
         * 安全设置的签名密钥
         */
        private String secret;
    }
    
    /**
     * 企业微信机器人配置项
     */
    @Data 
    public static class WeChatRobotConfig {
        /**
         * webhook地址
         */
        private String webhook;
        /**
         * 机器人的key
         */
        private String key;
    }
    
    /**
     * 邮件机器人配置项
     */
    @Data
    public static class EmailRobotConfig {
        /**
         * SMTP服务器地址
         */
        private String host;
        /**
         * SMTP服务器端口
         */
        private Integer port;
        /**
         * 邮箱账号
         */
        private String username;
        /**
         * 邮箱密码或授权码
         */
        private String password;
        /**
         * 发件人地址
         */
        private String from;
        /**
         * 是否启用SSL
         */
        private Boolean ssl = true;
    }

    /**
     * 短信配置项
     */
    @Data
    public static class SmsRobotConfig {
        /**
         * 短信提供商
         */
        private String provider;
        
        /**
         * 短信服务商接口地址
         */
        private String endpoint;
        
        /**
         * 短信服务商分配的accessKey
         */
        private String accessKey;
        
        /**
         * 短信服务商分配的secretKey
         */
        private String secretKey;
        
        /**
         * 短信签名
         */
        private String signName;
        
        /**
         * 短信模板ID
         */
        private String templateId;
    }

    @Override
    public void afterPropertiesSet() {
        refreshRobotClients();
    }

    /**
     * 刷新机器人客户端
     */
    private synchronized void refreshRobotClients() {
        try {
            // 清空现有客户端
            robotClients.clear();

            // 重新创建钉钉机器人客户端
            getDingtalk().forEach((id, config) -> {
                robotClients.put(id, new DingTalkRobotClient(
                        config.getWebhook(),
                        config.getSecret()
                ));
            });

            // 重新创建微信机器人客户端
            getWechat().forEach((id, config) -> {
                robotClients.put(id, new WeChatRobotClient(
                        config.getWebhook(),
                        config.getKey()
                ));
            });

            // 重新创建邮件机器人客户端
            getEmail().forEach((id, config) -> {
                robotClients.put(id, new EmailRobotClient(
                        config.getHost(),
                        config.getPort(),
                        config.getUsername(),
                        config.getPassword(),
                        config.getFrom(),
                        config.getSsl()
                ));
            });

            // 重新创建短信机器人客户端
            getSms().forEach((id, config) -> {
                robotClients.put(id, new SmsRobotClient(
                        config.getEndpoint(),
                        config.getAccessKey(),
                        config.getSecretKey(),
                        config.getSignName(),
                        config.getTemplateId()
                ));
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to refresh robot clients", e);
        }
    }

    /**
     * wechat 根据key 获取对应的配置
     */
    public WeChatRobotConfig getWeChatRobotConfig(String key) {
        return wechat.get(key);
    }

    /**
     * 获取机器人客户端映射
     *
     * @return 机器人客户端映射
     */
    public Map<String, RobotClient> getRobotClients() {
        return robotClients;
    }
} 