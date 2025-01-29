package io.github.kk01001.robot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信脚本配置属性
 * 用于配置不同短信提供商的Groovy脚本
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "sms")
public class SmsScriptProperties {
    
    /**
     * 短信脚本配置
     * key: 短信提供商标识
     * value: Groovy脚本内容
     */
    private Map<String, String> scripts = new HashMap<>();
    
    /**
     * 获取指定提供商的脚本
     *
     * @param provider 提供商标识
     * @return 脚本内容，如果不存在返回null
     */
    public String getScript(String provider) {
        return scripts.get(provider);
    }
} 