package io.github.kk01001.common.dict.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dict")
public class DictProperties {

    /**
     * 是否启用字典功能
     */
    private Boolean enabled = false;

    /**
     * 是否在启动时自动刷新字典
     */
    private Boolean autoRefresh = true;
} 