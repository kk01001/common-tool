package io.github.kk01001.idempotent;

import io.github.kk01001.idempotent.aspect.IdempotentAspect;
import io.github.kk01001.idempotent.config.IdempotentProperties;
import io.github.kk01001.idempotent.core.IdempotentKeyGenerator;
import io.github.kk01001.idempotent.core.RedisIdempotentExecutor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 幂等自动配置
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(IdempotentProperties.class)
@ConditionalOnProperty(prefix = "idempotent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IdempotentConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdempotentKeyGenerator idempotentKeyGenerator() {
        return new IdempotentKeyGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisIdempotentExecutor redisIdempotentExecutor(RedissonClient redissonClient,
                                                          IdempotentProperties properties) {
        return new RedisIdempotentExecutor(redissonClient, properties);
    }

    @Bean
    public IdempotentAspect idempotentAspect(RedisIdempotentExecutor executor,
                                            IdempotentKeyGenerator keyGenerator,
                                            ApplicationContext applicationContext) {
        return new IdempotentAspect(executor, keyGenerator, applicationContext);
    }
}
