package io.github.kk01001.design.pattern.statemachine;

import io.github.kk01001.design.pattern.statemachine.core.StateMachineBuilder;
import io.github.kk01001.design.pattern.statemachine.persister.InMemoryStatePersister;
import io.github.kk01001.design.pattern.statemachine.persister.StatePersister;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * @date 2024-04-07 14:31:00
 * @description 状态机自动配置类
 */
@Configuration
public class StateMachineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StateMachineBuilder<Object, Object, Object> stateMachineBuilder() {
        return new StateMachineBuilder<>();
    }

    @Bean
    @ConditionalOnMissingBean
    public StateMachineFactory stateMachineFactory() {
        return new StateMachineFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public StatePersister<Object, Object> localStatePersister() {
        return new InMemoryStatePersister<>();
    }

}