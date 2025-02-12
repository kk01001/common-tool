package io.github.kk01001.crypto.config;

import io.github.kk01001.crypto.enums.CryptoType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

@Data
@RefreshScope
@ConfigurationProperties(prefix = "common.params.crypto")
public class ParamsCryptoProperties {

    /**
     * 加密方式
     */
    private CryptoType type = CryptoType.AES;

    /**
     * 对称加密密钥
     */
    private String key = "defaultKey123456";

    /**
     * RSA公钥
     */
    private String publicKey;

    /**
     * RSA私钥
     */
    private String privateKey;

    /**
     * mybatis 加密的字段名称 查询时
     */
    private List<String> mybatisCryptoColumn;
} 