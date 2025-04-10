# Robot Message Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/robot-message-spring-boot-starter.svg)](https://search.maven.org/artifact/io.github.kk01001/robot-message-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

## 简介

`robot-message-spring-boot-starter` 是一个用于简化多平台消息通知的 Spring Boot Starter。它提供了统一的 API 来发送各种类型的消息到不同的通知平台，例如钉钉机器人、企业微信机器人、邮件和短信等。通过简单的配置，即可在 Spring Boot 应用中快速集成消息通知功能。

## 功能特点

- 🔌 **多平台支持**：支持钉钉机器人、企业微信机器人、邮件、短信等多种消息通知渠道
- 🔄 **统一 API**：提供统一的接口发送各类消息，无需关注不同平台的实现细节
- 📝 **丰富的消息类型**：支持文本、图片、链接、Markdown、卡片、新闻等多种消息格式
- 🧩 **可扩展性**：易于扩展以支持更多的消息平台和消息类型
- 🔁 **配置热更新**：支持通过 Nacos 等配置中心动态更新机器人配置
- 🧠 **脚本支持**：集成 Groovy 脚本引擎，支持灵活的消息内容生成逻辑
- 🔄 **并发处理**：支持多机器人实例并行处理消息发送任务

## 技术栈

- Java 21
- Spring Boot 3.x
- Groovy 4.x
- Spring Boot Mail

## 快速开始

### 添加依赖

在你的 `pom.xml` 文件中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>robot-message-spring-boot-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

### 配置属性

在 `application.properties` 或 `application.yml` 中配置：

```yaml
robot:
  # 钉钉机器人配置
  dingtalk:
    robot1:  # 机器人ID，可自定义
      webhook: https://oapi.dingtalk.com/robot/send?access_token=xxx
      secret: SECxxx  # 安全设置的签名密钥
    robot2:
      webhook: https://oapi.dingtalk.com/robot/send?access_token=yyy
      secret: SECyyy
      
  # 企业微信机器人配置
  wechat:
    wechat1:  # 机器人ID，可自定义
      webhook: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx
      key: xxx
    wechat2:
      webhook: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=yyy
      key: yyy
  
  # 邮件配置
  email:
    mail1:  # 机器人ID，可自定义
      host: smtp.example.com
      port: 465
      username: your-email@example.com
      password: your-password-or-token
      from: your-email@example.com
      ssl: true
  
  # 短信配置
  sms:
    sms1:  # 机器人ID，可自定义
      provider: aliyun  # 短信提供商
      endpoint: https://dysmsapi.aliyuncs.com
      access-key: your-access-key
      secret-key: your-secret-key
      sign-name: 您的签名
      template-id: SMS_XXXXXXXXX
```

## 使用示例

### 发送文本消息

```java
import io.github.kk01001.robot.message.TextMessage;
import io.github.kk01001.robot.service.RobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class NotificationService {

    @Autowired
    private RobotService robotService;
    
    public void sendTextNotification() {
        // 创建文本消息
        TextMessage message = new TextMessage();
        message.setContent("这是一条测试消息");
        message.setAtMobiles(Arrays.asList("13800138000", "13900139000"));
        message.setAtAll(false);
        
        // 发送到指定的机器人
        robotService.sendMessage("robot1", message);  // 使用钉钉机器人
        robotService.sendMessage("wechat1", message); // 使用企业微信机器人
    }
}
```

### 发送链接消息

```java
import io.github.kk01001.robot.message.LinkMessage;
import io.github.kk01001.robot.service.RobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private RobotService robotService;
    
    public void sendLinkNotification() {
        // 创建链接消息
        LinkMessage message = new LinkMessage();
        message.setTitle("Spring Boot 3.0 发布了");
        message.setText("Spring Boot 3.0 包含了许多新特性和改进...");
        message.setPicUrl("https://spring.io/images/spring-logo.svg");
        message.setMessageUrl("https://spring.io/blog/2022/05/19/spring-boot-3-0-0-m3-available-now");
        
        // 发送到钉钉机器人
        robotService.sendMessage("robot1", message);
    }
}
```

### 发送 Markdown 消息

```java
import io.github.kk01001.robot.message.MarkdownMessage;
import io.github.kk01001.robot.service.RobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class NotificationService {

    @Autowired
    private RobotService robotService;
    
    public void sendMarkdownNotification() {
        // 创建 Markdown 消息
        MarkdownMessage message = new MarkdownMessage();
        message.setTitle("系统警告");
        message.setText("### 警告通知\n" +
                "> 服务器 CPU 使用率超过 90%\n\n" +
                "**时间**：2023-06-01 12:30:45\n" +
                "**服务器**：prod-server-01\n" +
                "**详情**：[点击查看详情](http://monitor.example.com)");
        message.setAtMobiles(Arrays.asList("13800138000"));
        
        // 发送到钉钉机器人
        robotService.sendMessage("robot1", message);
    }
}
```

### 发送邮件消息

```java
import io.github.kk01001.robot.message.EmailMessage;
import io.github.kk01001.robot.service.RobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class NotificationService {

    @Autowired
    private RobotService robotService;
    
    public void sendEmailNotification() {
        // 创建邮件消息
        EmailMessage message = new EmailMessage();
        message.setSubject("系统通知");
        message.setTo(Arrays.asList("user1@example.com", "user2@example.com"));
        message.setCc(Arrays.asList("admin@example.com"));
        message.setContent("<h1>系统通知</h1><p>这是一封系统自动发送的通知邮件</p>");
        message.setHtml(true);
        
        // 发送邮件
        robotService.sendMessage("mail1", message);
    }
}
```

### 发送短信消息

```java
import io.github.kk01001.robot.message.SmsMessage;
import io.github.kk01001.robot.service.RobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private RobotService robotService;
    
    public void sendSmsNotification() {
        // 创建短信消息
        SmsMessage message = new SmsMessage();
        message.setPhoneNumbers(Arrays.asList("13800138000", "13900139000"));
        
        // 设置模板参数
        Map<String, String> params = new HashMap<>();
        params.put("code", "123456");
        params.put("product", "示例应用");
        message.setTemplateParams(params);
        
        // 发送短信
        robotService.sendMessage("sms1", message);
    }
}
```

## 支持的消息类型

### 通用消息类型

- **TextMessage**: 文本消息
- **MarkdownMessage**: Markdown 格式消息
- **LinkMessage**: 链接消息
- **ImageMessage**: 图片消息
- **FileMessage**: 文件消息

### 钉钉专属消息类型

- **ActionCardMessage**: 钉钉交互式卡片消息
- **FeedCardMessage**: 钉钉多条信息卡片消息
- **VoiceMessage**: 钉钉语音消息

### 其他类型

- **EmailMessage**: 邮件消息
- **SmsMessage**: 短信消息
- **NewsMessage**: 图文消息（适用于企业微信）

## 高级特性

### 媒体上传与处理

```java
import io.github.kk01001.robot.service.MediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaService {

    @Autowired
    private MediaUploadService mediaUploadService;
    
    public String uploadMedia(MultipartFile file, String type) {
        // 上传媒体文件并返回媒体ID
        return mediaUploadService.upload(file, type);
    }
}
```

### 集成 Groovy 脚本

```java
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ScriptService {

    public String evaluateTemplate(String template, Map<String, Object> params) {
        // 创建绑定变量
        Binding binding = new Binding();
        if (params != null) {
            params.forEach(binding::setVariable);
        }
        
        // 创建Groovy Shell
        GroovyShell shell = new GroovyShell(binding);
        
        // 执行脚本
        return shell.evaluate("\"" + template + "\"").toString();
    }
    
    public void demo() {
        String template = "Hello, ${name}! Today is ${new Date().format('yyyy-MM-dd')}";
        Map<String, Object> params = new HashMap<>();
        params.put("name", "World");
        
        String result = evaluateTemplate(template, params);
        System.out.println(result); // 输出: Hello, World! Today is 2023-05-23
    }
}
```

### 自定义消息类型

```java
import io.github.kk01001.robot.message.RobotMessage;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CustomMessage implements RobotMessage {

    private String customField1;
    private String customField2;
    
    @Override
    public String getType() {
        return "custom";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", getType());
        
        Map<String, Object> custom = new HashMap<>();
        custom.put("field1", customField1);
        custom.put("field2", customField2);
        
        message.put("custom", custom);
        return message;
    }
    
    @Override
    public Map<String, Object> toMessageMap(String robotType) {
        // 根据不同的机器人类型，返回适配的消息格式
        if ("wechat".equals(robotType)) {
            // 企业微信的格式
            // ...
        }
        return toMessageMap();
    }
}
```

## 最佳实践

### 配置管理

1. **机器人分组**：按照业务线或功能模块分组配置机器人，便于管理
   ```yaml
   robot:
     dingtalk:
       order-notification:  # 订单通知机器人
         webhook: https://oapi.dingtalk.com/robot/send?access_token=xxx
       system-alert:        # 系统警告机器人
         webhook: https://oapi.dingtalk.com/robot/send?access_token=yyy
   ```

2. **使用配置中心**：利用 Nacos 等配置中心动态管理机器人配置
   ```java
   @RefreshScope  // 通过Nacos实现配置热更新
   @RestController
   public class NotificationController {
       @Autowired
       private RobotService robotService;
       // ...
   }
   ```

### 消息发送

1. **异步发送**：对于非关键路径的通知，建议使用异步方式发送
   ```java
   @Async
   public CompletableFuture<Void> sendNotificationAsync(String robotId, RobotMessage message) {
       robotService.sendMessage(robotId, message);
       return CompletableFuture.completedFuture(null);
   }
   ```

2. **错误处理**：添加适当的错误处理逻辑
   ```java
   try {
       robotService.sendMessage(robotId, message);
   } catch (Exception e) {
       log.error("Failed to send message to robot {}: {}", robotId, e.getMessage());
       // 可以考虑重试或者降级到其他通知渠道
   }
   ```

3. **消息模板化**：将常用的消息模板化，提高复用性
   ```java
   public TextMessage createAlertMessage(String title, String content, boolean isUrgent) {
       TextMessage message = new TextMessage();
       message.setContent(String.format("[%s] %s\n%s", 
           isUrgent ? "紧急" : "普通", title, content));
       message.setAtAll(isUrgent);
       return message;
   }
   ```

## 常见问题

### Q: 钉钉机器人发送消息时报错 "invalid timestamp"
A: 检查服务器时间是否准确，钉钉机器人要求请求的时间戳与服务器时间相差不能超过1小时。

### Q: 企业微信机器人消息没有收到
A: 确认webhook地址是否正确，并且检查消息格式是否符合企业微信的要求。

### Q: 如何修改短信模板？
A: 短信模板需要在短信服务商平台上创建并审核通过，然后在配置中指定对应的模板ID。

### Q: 发送邮件报错 "身份验证失败"
A: 检查邮箱配置的用户名和密码是否正确，对于部分邮件服务商，需要使用应用专用密码而不是账户密码。

## 许可证

本项目遵循 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源许可证。

## 贡献指南

欢迎提交问题和贡献代码，请通过 GitHub Issue 和 Pull Request 参与项目开发。 