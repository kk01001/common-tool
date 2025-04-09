package io.github.kk01001.design.pattern.statemachine.config;

import io.github.kk01001.design.pattern.statemachine.persister.InMemoryStatePersister;
import io.github.kk01001.design.pattern.statemachine.persister.RedissonStatePersister;
import io.github.kk01001.design.pattern.statemachine.persister.StatePersister;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * @date 2024-04-10 16:30:00
 * @description 状态机持久化自动配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(StateMachineProperties.class)
public class StateMachinePersisterAutoConfiguration {

    /**
     * 配置 Redis 持久化实现
     */
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnProperty(prefix = "state-machine.persister", name = "type", havingValue = "redisson")
    public StatePersister<?, ?> redissonStatePersister(RedissonClient redissonClient, StateMachineProperties properties) {
        log.info("Configuring Redisson state persister for state machine");
        return new RedissonStatePersister<>(redissonClient, properties);
    }

    /**
     * 配置内存持久化实现（默认）
     */
    @Bean
    @ConditionalOnMissingBean(StatePersister.class)
    public StatePersister<?, ?> inMemoryStatePersister() {
        log.info("Configuring in-memory state persister for state machine");
        return new InMemoryStatePersister<>();
    }
}
