package io.github.kk01001.docs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {
    
    /**
     * 是否启用swagger
     */
    private Boolean enabled = true;
    
    /**
     * 文档标题
     */
    private String title = "API文档";
    
    /**
     * 文档描述
     */
    private String description = "API接口文档";
    
    /**
     * 版本
     */
    private String version = "1.0.0";
    
    /**
     * 联系人姓名
     */
    private String contactName;
    
    /**
     * 联系人邮箱
     */
    private String contactEmail;
    
    /**
     * 联系人网址
     */
    private String contactUrl;

    /**
     * 是否开启Basic认证
     */
    private Boolean basicAuth = false;

    /**
     * Basic认证用户名
     */
    private String username = "admin";

    /**
     * Basic认证密码
     */
    private String password = "123456";
} 