package io.github.kk01001.robot.client;

import io.github.kk01001.robot.message.EmailMessage;
import io.github.kk01001.robot.message.RobotMessage;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Properties;

/**
 * 邮件机器人客户端
 * 负责处理邮件发送的具体实现，支持HTML内容、多收件人、抄送和密送
 */
@Slf4j
public class EmailRobotClient implements RobotClient {

    /**
     * Spring邮件发送器
     */
    private final JavaMailSender mailSender;

    /**
     * 发件人邮箱地址
     */
    private final String senderEmailAddress;

    @Override
    public String getRobotType() {
        return "email";
    }

    /**
     * 创建邮件机器人客户端
     *
     * @param smtpHost SMTP服务器地址
     * @param smtpPort SMTP服务器端口
     * @param emailUsername 邮箱账号
     * @param emailPassword 邮箱密码或授权码
     * @param senderEmailAddress 发件人邮箱地址
     * @param enableSSL 是否启用SSL加密
     */
    public EmailRobotClient(String smtpHost, Integer smtpPort, String emailUsername, 
                          String emailPassword, String senderEmailAddress, Boolean enableSSL) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(smtpHost);
        sender.setPort(smtpPort);
        sender.setUsername(emailUsername);
        sender.setPassword(emailPassword);
        
        // 配置邮件服务器属性
        Properties mailProperties = sender.getJavaMailProperties();
        mailProperties.put("mail.transport.protocol", "smtp");
        mailProperties.put("mail.smtp.auth", "true");
        if (enableSSL) {
            mailProperties.put("mail.smtp.ssl.enable", "true");
            mailProperties.put("mail.smtp.ssl.trust", smtpHost);
        }
        // 设置超时时间
        mailProperties.put("mail.smtp.timeout", "10000");
        mailProperties.put("mail.smtp.connectiontimeout", "10000");
        
        this.mailSender = sender;
        this.senderEmailAddress = senderEmailAddress;
    }

    @Override
    public void sendMessage(RobotMessage message) {
        try {
            if (!(message instanceof EmailMessage)) {
                throw new IllegalArgumentException("消息类型必须是EmailMessage");
            }
            
            EmailMessage emailMessage = (EmailMessage) message;
            sendEmailMessage(emailMessage);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }
    
    /**
     * 发送邮件消息
     *
     * @param emailMessage 邮件消息对象
     */
    private void sendEmailMessage(EmailMessage emailMessage) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        
        // 设置基本信息
        messageHelper.setFrom(senderEmailAddress);
        messageHelper.setSubject(emailMessage.getEmailSubject());
        messageHelper.setText(emailMessage.getEmailContent(), emailMessage.getIsHtmlFormat());
        
        // 设置收件人
        if (emailMessage.getRecipientEmails() != null && !emailMessage.getRecipientEmails().isEmpty()) {
            messageHelper.setTo(emailMessage.getRecipientEmails().toArray(new String[0]));
        } else {
            throw new IllegalArgumentException("收件人不能为空");
        }
        
        // 设置抄送
        if (emailMessage.getCarbonCopyEmails() != null && !emailMessage.getCarbonCopyEmails().isEmpty()) {
            messageHelper.setCc(emailMessage.getCarbonCopyEmails().toArray(new String[0]));
        }
        
        // 设置密送
        if (emailMessage.getBlindCarbonCopyEmails() != null && !emailMessage.getBlindCarbonCopyEmails().isEmpty()) {
            messageHelper.setBcc(emailMessage.getBlindCarbonCopyEmails().toArray(new String[0]));
        }
        
        // 发送邮件
        mailSender.send(mimeMessage);
        log.info("邮件发送成功，收件人: {}", String.join(",", emailMessage.getRecipientEmails()));
    }
} 