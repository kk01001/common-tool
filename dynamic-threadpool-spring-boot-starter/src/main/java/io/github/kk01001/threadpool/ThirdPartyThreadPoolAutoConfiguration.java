package io.github.kk01001.threadpool;

import io.github.kk01001.threadpool.registry.ThreadPoolRegistry;
import io.github.kk01001.threadpool.thirdparty.ThirdPartyThreadPoolProperties;
import io.github.kk01001.threadpool.thirdparty.initializer.ThirdPartyThreadPoolInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
    public ThirdPartyThreadPoolInitializer thirdPartyThreadPoolInitializer(
            ThirdPartyThreadPoolProperties properties,
            ThreadPoolRegistry registry) {
        log.info("Initializing ThirdPartyThreadPoolInitializer");
        return new ThirdPartyThreadPoolInitializer(properties, registry);
    }
}
