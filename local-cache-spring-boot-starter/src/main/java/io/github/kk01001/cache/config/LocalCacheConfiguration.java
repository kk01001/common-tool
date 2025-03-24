package io.github.kk01001.cache.config;

import io.github.kk01001.cache.factory.LocalCaffeineCacheFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description
 */
@Configuration(proxyBeanMethods = false)
public class LocalCacheConfiguration {

    @Bean
    public LocalCaffeineCacheFactory localCaffeineCacheFactory() {
        return new LocalCaffeineCacheFactory();
    }
}
