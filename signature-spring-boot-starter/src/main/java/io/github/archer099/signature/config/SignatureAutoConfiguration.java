package io.github.archer099.signature.config;

import io.github.archer099.signature.handler.SignatureFilter;
import io.github.archer099.signature.handler.SignatureHandler;
import io.github.archer099.signature.handler.SignatureInterceptor;
import io.github.archer099.signature.properties.SignatureProperties;
import io.github.archer099.signature.store.AppSecretStore;
import io.github.archer099.signature.store.ConfigAppSecretStore;
import io.github.archer099.signature.store.InMemoryNonceStore;
import io.github.archer099.signature.store.NonceStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description 签名自动配置类
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SignatureProperties.class)
@ConditionalOnProperty(prefix = "signature", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SignatureAutoConfiguration {
    
    /**
     * Nonce 存储（默认内存实现，可被用户覆盖）
     */
    @Bean
    @ConditionalOnMissingBean
    public NonceStore nonceStore(SignatureProperties properties) {
        log.info("初始化 NonceStore（内存实现），过期时间：{}", properties.getNonceTtl());
        return new InMemoryNonceStore(properties.getNonceTtl());
    }
    
    /**
     * AppSecret 存储（默认配置实现，可被用户覆盖）
     */
    @Bean
    @ConditionalOnMissingBean
    public AppSecretStore appSecretStore(SignatureProperties properties) {
        log.info("初始化 AppSecretStore（配置实现），应用数量：{}", properties.getApps().size());
        return new ConfigAppSecretStore(properties);
    }
    
    /**
     * 签名处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public SignatureHandler signatureHandler(SignatureProperties properties, 
                                              AppSecretStore appSecretStore, 
                                              NonceStore nonceStore) {
        log.info("初始化 SignatureHandler，算法：{}", properties.getAlgorithm());
        return new SignatureHandler(properties, appSecretStore, nonceStore);
    }
    
    /**
     * 签名拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public SignatureInterceptor signatureInterceptor(SignatureProperties properties,
                                                      SignatureHandler signatureHandler) {
        log.info("初始化 SignatureInterceptor");
        return new SignatureInterceptor(properties, signatureHandler);
    }
    
    /**
     * 签名过滤器（用于支持重复读取请求体）
     */
    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<SignatureFilter> signatureFilterRegistration() {
        FilterRegistrationBean<SignatureFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SignatureFilter());
        registration.addUrlPatterns("/*");
        registration.setName("signatureFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        log.info("初始化 SignatureFilter");
        return registration;
    }
    
    /**
     * Web MVC 配置
     */
    @Bean
    public WebMvcConfigurer signatureWebMvcConfigurer(SignatureProperties properties,
                                                       SignatureInterceptor signatureInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                log.info("注册 SignatureInterceptor，排除路径：{}", properties.getExcludePaths());
                registry.addInterceptor(signatureInterceptor)
                        .addPathPatterns("/**")
                        .excludePathPatterns(properties.getExcludePaths().toArray(new String[0]));
            }
        };
    }
}
