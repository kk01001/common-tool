package io.github.kk01001.robot.message;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件消息
 * 用于发送邮件的消息格式，支持HTML内容、多收件人、抄送和密送
 */
@Data
public class EmailMessage implements RobotMessage {
    
    /**
     * 邮件主题
     */
    private String emailSubject;
    
    /**
     * 邮件正文
     * 可以是纯文本或HTML格式
     */
    private String emailContent;
    
    /**
     * 收件人邮箱地址列表
     */
    private List<String> recipientEmails;
    
    /**
     * 抄送人邮箱地址列表
     */
    private List<String> carbonCopyEmails;
    
    /**
     * 密送人邮箱地址列表
     */
    private List<String> blindCarbonCopyEmails;
    
    /**
     * 是否为HTML格式
     * true: 内容将以HTML格式发送
     * false: 内容将以纯文本格式发送
     */
    private Boolean isHtmlFormat = true;
    
    @Override
    public String getType() {
        return "email";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "email");
        
        // 邮件基本信息
        Map<String, Object> email = new HashMap<>();
        email.put("subject", emailSubject);
        email.put("content", emailContent);
        email.put("html", isHtmlFormat);
        message.put("email", email);
        
        // 收件人信息
        Map<String, Object> recipients = new HashMap<>();
        recipients.put("atMobiles", recipientEmails);      // 主要收件人
        recipients.put("atUserIds", carbonCopyEmails);     // 抄送人
        recipients.put("bcc", blindCarbonCopyEmails);      // 密送人
        message.put("at", recipients);
        
        return message;
    }

    @Override
    public Map<String, Object> toMessageMap(String robotType) {
        return Map.of();
    }
}