package io.github.kk01001.common.desensitize.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "desensitize")
public class DesensitizeProperties {
    
    /**
     * 是否启用脱敏功能
     */
    private Boolean enabled = false;
    
    /**
     * 是否启用Jackson序列化脱敏
     */
    private Boolean enableJackson = true;
    
    /**
     * 是否启用FastJson序列化脱敏
     */
    private Boolean enableFastjson = false;
    
    /**
     * 是否启用MyBatis结果集脱敏
     */
    private Boolean enableMybatis = false;
} 