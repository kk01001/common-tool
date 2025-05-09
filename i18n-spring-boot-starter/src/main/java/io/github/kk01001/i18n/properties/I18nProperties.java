package io.github.kk01001.i18n.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "i18n")
public class I18nProperties {
    
    /**
     * 是否启用国际化
     */
    private Boolean enabled = false;
    
    /**
     * 默认语言
     */
    private String defaultLocale = "zh_CN";
    
    /**
     * 国际化资源文件路径
     */
    private String basename = "i18n/messages";
    
    /**
     * 消息缓存过期时间(秒)，-1表示永不过期
     */
    private Integer cacheSeconds = 3600;
    
    /**
     * 是否总是使用消息格式化
     */
    private Boolean alwaysUseMessageFormat = false;
    
    /**
     * 是否使用代码作为默认消息
     */
    private Boolean useCodeAsDefaultMessage = true;
    
    /**
     * 消息提供者类型：memory-内存, resource-资源文件
     */
    private String provider = "resource";
} 