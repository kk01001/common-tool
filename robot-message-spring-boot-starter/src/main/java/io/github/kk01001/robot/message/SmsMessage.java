package io.github.kk01001.robot.message;

import lombok.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短信消息
 * 用于发送短信通知
 */
@Data
public class SmsMessage implements RobotMessage {

    /**
     * 短信提供商
     */
    private String provider;
    
    /**
     * 接收短信的手机号列表
     */
    private List<String> phoneNumbers;
    
    /**
     * 短信模板参数
     * key: 参数名
     * value: 参数值
     */
    private Map<String, String> templateParams = new HashMap<>();
    
    /**
     * 短信内容(当不使用模板时)
     */
    private String content;
    
    @Override
    public String getType() {
        return "sms";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "sms");
        
        Map<String, Object> sms = new HashMap<>();
        sms.put("phoneNumbers", phoneNumbers);
        sms.put("templateParams", templateParams);
        sms.put("content", content);
        message.put("sms", sms);
        
        return message;
    }
} 