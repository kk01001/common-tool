package io.github.kk01001.design;

import io.github.kk01001.design.pattern.responsibility.ResponsibilityChainFactory;
import io.github.kk01001.design.pattern.strategy.StrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 配置并注册设计模式相关的Bean
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class DesignPatternConfiguration {

    @Bean
    public ResponsibilityChainFactory responsibilityChainFactory() {
        return new ResponsibilityChainFactory();
    }

    @Bean
    public StrategyFactory strategyFactory() {
        return new StrategyFactory();
    }
}
