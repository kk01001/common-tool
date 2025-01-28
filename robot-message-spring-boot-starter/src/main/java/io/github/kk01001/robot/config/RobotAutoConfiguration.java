package io.github.kk01001.robot.config;

import io.github.kk01001.robot.client.*;
import io.github.kk01001.robot.service.RobotService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 机器人自动配置类
 * 负责创建和配置机器人相关的Bean
 */
@ComponentScan(basePackages = "io.github.kk01001.robot")
@Configuration
@EnableConfigurationProperties(RobotProperties.class)
@ConditionalOnClass(RestTemplate.class)
public class RobotAutoConfiguration {

    /**
     * 创建RestTemplate Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * 创建机器人客户端映射
     */
    @Bean
    public Map<String, RobotClient> robotClients(
            RobotProperties properties,
            RestTemplate restTemplate) {
            
        var clients = new HashMap<String, RobotClient>();
        
        // 配置钉钉机器人
        properties.getDingtalk().forEach((id, config) -> {
            clients.put(id, new DingTalkRobotClient(
                config.getWebhook(),
                config.getSecret(),
                restTemplate
            ));
        });
        
        // 配置微信机器人
        properties.getWechat().forEach((id, config) -> {
            clients.put(id, new WeChatRobotClient(
                config.getWebhook(),
                    config.getKey(),
                restTemplate
            ));
        });
        
        // 配置邮件机器人
        properties.getEmail().forEach((id, config) -> {
            clients.put(id, new EmailRobotClient(
                config.getHost(),
                config.getPort(),
                config.getUsername(),
                config.getPassword(),
                config.getFrom(),
                config.getSsl()
            ));
        });
        
        // 配置短信机器人
        properties.getSms().forEach((id, config) -> {
            clients.put(id, new SmsRobotClient(
                config.getEndpoint(),
                config.getAccessKey(),
                config.getSecretKey(),
                config.getSignName(),
                config.getTemplateId(),
                restTemplate
            ));
        });
        
        return clients;
    }
    
    /**
     * 创建机器人服务Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public RobotService robotService(Map<String, RobotClient> robotClients) {
        return new RobotService(robotClients);
    }


} 