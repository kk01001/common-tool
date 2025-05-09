package io.github.kk01001.crypto.config;

import io.github.kk01001.crypto.ParamsCryptoProvider;
import io.github.kk01001.crypto.annotation.CryptoField;
import io.github.kk01001.crypto.enums.CryptoType;
import io.github.kk01001.crypto.exception.CryptoException;
import io.github.kk01001.crypto.mybatis.MybatisParamsEncryptInterceptor;
import io.github.kk01001.crypto.mybatis.MybatisQueryParamEncryptInterceptor;
import io.github.kk01001.crypto.mybatis.MybatisQueryResultDecryptInterceptor;
import io.github.kk01001.crypto.provider.AESParamsCryptoProvider;
import io.github.kk01001.crypto.provider.RSAParamsCryptoProvider;
import io.github.kk01001.crypto.provider.SM2ParamsCryptoProvider;
import io.github.kk01001.crypto.provider.SM4ParamsCryptoProvider;
import io.github.kk01001.crypto.web.ParamsCryptoRequestBodyAdvice;
import io.github.kk01001.crypto.web.ParamsCryptoResponseBodyAdvice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(ParamsCryptoProperties.class)
public class ParamsCryptoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ParamsCryptoProvider paramsCryptoProvider(ParamsCryptoProperties properties) {
        CryptoType type = properties.getType();
        ParamsCryptoProvider provider = switch (type) {
            case AES -> new AESParamsCryptoProvider(properties.getKey());
            case SM4 -> new SM4ParamsCryptoProvider(properties.getKey());
            case RSA, SM2 -> {
                if (!StringUtils.hasText(properties.getPublicKey()) || !StringUtils.hasText(properties.getPrivateKey())) {
                    throw new CryptoException(type.name() + "加密需要配置公钥和私钥");
                }
                yield switch (type) {
                    case RSA -> new RSAParamsCryptoProvider(properties.getPrivateKey(), properties.getPublicKey());
                    case SM2 -> new SM2ParamsCryptoProvider(properties.getPrivateKey(), properties.getPublicKey());
                    default -> throw new IllegalStateException("Unexpected value: " + type);
                };
            }
        };

        // 初始化时刷新配置
        refreshProvider(provider, properties);
        return provider;
    }

    private void refreshProvider(ParamsCryptoProvider provider, ParamsCryptoProperties properties) {
        CryptoType type = properties.getType();
        switch (type) {
            case RSA, SM2 -> provider.refreshRSAKey(properties.getPublicKey(), properties.getPrivateKey());
            case AES, SM4 -> provider.refreshKey(properties.getKey());
        }
    }

    @Bean
    public ParamsCryptoRequestBodyAdvice paramsCryptoRequestBodyAdvice(ParamsCryptoProvider paramsCryptoProvider) {
        return new ParamsCryptoRequestBodyAdvice(paramsCryptoProvider);
    }

    @Bean
    public ParamsCryptoResponseBodyAdvice paramsCryptoResponseBodyAdvice(ParamsCryptoProvider paramsCryptoProvider) {
        return new ParamsCryptoResponseBodyAdvice(paramsCryptoProvider);
    }

    @Bean
    public ParamsCryptoRefreshListener paramsCryptoRefreshListener(
            ParamsCryptoProvider paramsCryptoProvider,
            ParamsCryptoProperties properties) {
        return new ParamsCryptoRefreshListener(paramsCryptoProvider, properties);
    }

    /**
     * mybatis 入库数据加密
     * 返回值需要是实体类 并使用注解
     *
     * @see CryptoField
     */
    @Bean
    @Order()
    public MybatisParamsEncryptInterceptor mybatisParamsEncryptInterceptor(ParamsCryptoProvider paramsCryptoProvider) {
        return new MybatisParamsEncryptInterceptor(paramsCryptoProvider);
    }

    /**
     * mybatis 查询结果解密
     * 返回值需要是实体类 并使用注解
     *
     * @see CryptoField
     */
    @Bean
    @Order()
    public MybatisQueryResultDecryptInterceptor mybatisQueryResultDecryptInterceptor(ParamsCryptoProvider paramsCryptoProvider) {
        return new MybatisQueryResultDecryptInterceptor(paramsCryptoProvider);
    }

    /**
     * mybatis 查询条件解密 指定字段
     */
    @Bean
    @Order()
    public MybatisQueryParamEncryptInterceptor queryParamInterceptor(ParamsCryptoProvider paramsCryptoProvider,
                                                                     ParamsCryptoProperties paramsCryptoProperties) {
        return new MybatisQueryParamEncryptInterceptor(paramsCryptoProvider, paramsCryptoProperties);
    }
} 