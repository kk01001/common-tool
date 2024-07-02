package io.github.kk01001;

import io.github.kk01001.core.ApplicationInfoInitialize;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * date:  2024-07-02 15:49
 */
@ComponentScan(basePackages = "io.github.kk01001")
@Configuration(proxyBeanMethods = false)
public class CommonToolConfiguration {

    @Bean
    public ApplicationInfoInitialize applicationInfoInitialize() {
        return new ApplicationInfoInitialize();
    }

}
