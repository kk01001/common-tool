package io.github.kk01001.robot.client;

import io.github.kk01001.robot.message.RobotMessage;
import io.github.kk01001.robot.message.SmsMessage;
import io.github.kk01001.robot.script.GroovyScriptExecutor;
import io.github.kk01001.robot.config.SmsScriptProperties;
import io.github.kk01001.robot.script.ScriptExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信机器人客户端
 * 通过Groovy脚本实现动态的短信发送功能
 */
@Slf4j
public class SmsRobotClient implements RobotClient {

    /**
     * Groovy脚本执行器
     */
    private final GroovyScriptExecutor scriptExecutor;
    
    /**
     * 短信配置参数
     */
    private final Map<String, Object> configParams;

    public SmsRobotClient(String endpoint, String accessKey, String secretKey, 
                         String signName, String templateId, RestTemplate restTemplate,
                         SmsScriptProperties scriptProperties) {
        this.scriptExecutor = new GroovyScriptExecutor(restTemplate, scriptProperties);
        
        // 初始化配置参数
        this.configParams = new HashMap<>();
        configParams.put("endpoint", endpoint);
        configParams.put("accessKey", accessKey);
        configParams.put("secretKey", secretKey);
        configParams.put("signName", signName);
        configParams.put("templateId", templateId);
    }

    @Override
    public String getRobotType() {
        return "sms";
    }

    @Override
    public void sendMessage(RobotMessage message) {
        try {
            if (!(message instanceof SmsMessage smsMessage)) {
                throw new IllegalArgumentException("消息类型必须是SmsMessage");
            }

            String provider = smsMessage.getProvider();
            if (provider == null || provider.isEmpty()) {
                throw new IllegalArgumentException("短信提供商不能为空");
            }
            
            // 构建脚本执行参数
            Map<String, Object> scriptParams = new HashMap<>(configParams);
            scriptParams.put("phoneNumbers", smsMessage.getPhoneNumbers());
            scriptParams.put("templateParams", smsMessage.getTemplateParams());
            scriptParams.put("content", smsMessage.getContent());
            scriptParams.put("provider", provider);
            // 执行对应提供商的脚本
            ScriptExecuteResult result = scriptExecutor.executeScript(provider, scriptParams);
            log.info("SMS sent result: {}", result.getResult());
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Failed to send SMS: " + result.getFailMessage());
            }
        } catch (Exception e) {
            log.error("Failed to send SMS", e);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
} 