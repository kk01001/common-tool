package io.github.kk01001.chat.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author kk01001
 * @date 2026-03-07 18:00:00
 * @description JWT 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "chat.jwt")
public class JwtProperties {

    /**
     * 签名密钥（Base64 编码）
     */
    private String secret;

    /**
     * Token 过期时间（毫秒），默认 24 小时
     */
    private long expiration = 86400000L;
}
