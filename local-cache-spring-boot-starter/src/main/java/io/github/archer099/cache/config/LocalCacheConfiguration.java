package io.github.archer099.cache.config;

import io.github.archer099.cache.factory.LocalCaffeineCacheFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author archer099
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
