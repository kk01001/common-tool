package io.github.kk01001.threadpool.thirdparty;

import io.github.kk01001.threadpool.config.DynamicThreadPoolProperties;
import io.github.kk01001.threadpool.registry.ThreadPoolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第三方线程池自动配置
 * 
 * @author kk01001
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "dynamic-threadpool.third-party", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ThirdPartyThreadPoolProperties.class)
public class ThirdPartyThreadPoolAutoConfiguration {
    
    @Bean
    public ThirdPartyThreadPoolManager thirdPartyThreadPoolManager(ApplicationContext applicationContext) {
        log.info("Initializing ThirdPartyThreadPoolManager");
        return new ThirdPartyThreadPoolManager(applicationContext);
    }
    
    @Bean
    public ThirdPartyThreadPoolInitializer thirdPartyThreadPoolInitializer(
            ThirdPartyThreadPoolManager manager,
            ThirdPartyThreadPoolProperties properties,
            ThreadPoolRegistry registry) {
        log.info("Initializing ThirdPartyThreadPoolInitializer");
        return new ThirdPartyThreadPoolInitializer(manager, properties, registry);
    }
}
