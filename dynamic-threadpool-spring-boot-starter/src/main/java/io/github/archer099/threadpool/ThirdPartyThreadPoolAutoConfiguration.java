package io.github.archer099.threadpool;

import io.github.archer099.threadpool.registry.ThreadPoolRegistry;
import io.github.archer099.threadpool.thirdparty.ThirdPartyThreadPoolProperties;
import io.github.archer099.threadpool.thirdparty.initializer.ThirdPartyThreadPoolInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第三方线程池自动配置
 * 
 * @author archer099
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "dynamic-threadpool.third-party", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ThirdPartyThreadPoolProperties.class)
public class ThirdPartyThreadPoolAutoConfiguration {

    @Bean
    public ThirdPartyThreadPoolInitializer thirdPartyThreadPoolInitializer(
            ThirdPartyThreadPoolProperties properties,
            ThreadPoolRegistry registry,
            ApplicationContext applicationContext) {
        log.info("Initializing ThirdPartyThreadPoolInitializer");
        return new ThirdPartyThreadPoolInitializer(properties, registry, applicationContext);
    }
}
