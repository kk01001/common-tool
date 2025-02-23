package io.github.kk01001.idempotent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 幂等配置属性
 */
@Data
@ConfigurationProperties(prefix = "idempotent")
public class IdempotentProperties {

    /**
     * 是否启用幂等性校验
     */
    private Boolean enabled = true;

    /**
     * key的默认前缀
     */
    private String keyPrefix = "idempotent:";

    /**
     * 默认过期时间(秒)
     */
    private Duration defaultExpireSeconds = Duration.ofSeconds(10);

    /**
     * 是否打印debug日志
     */
    private Boolean debugLog = false;
} 