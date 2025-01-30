package io.github.kk01001.common.dict.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.kk01001.common.dict.DictCache;
import io.github.kk01001.common.dict.DictLoader;
import io.github.kk01001.common.dict.DictRefresher;
import io.github.kk01001.common.dict.jackson.DictModule;
import io.github.kk01001.common.dict.jackson.DictSerializer;
import io.github.kk01001.common.dict.properties.DictProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DictProperties.class)
@ConditionalOnProperty(prefix = "dict", name = "enabled", havingValue = "true")
public class DictAutoConfiguration {

    @Bean
    public DictModule dictModule(DictLoader dictLoader) {
        DictCache.init(dictLoader);  // 初始化DictCache
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new DictSerializer());
        return new DictModule(module);
    }

    @Bean
    public DictRefresher dictRefresher(DictProperties properties) {
        return new DictRefresher(properties);
    }
} 