package io.github.archer099.dict.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.archer099.dict.DictCache;
import io.github.archer099.dict.DictLoader;
import io.github.archer099.dict.DictRefresher;
import io.github.archer099.dict.jackson.DictModule;
import io.github.archer099.dict.jackson.DictSerializer;
import io.github.archer099.dict.properties.DictProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableConfigurationProperties(DictProperties.class)
@ConditionalOnProperty(prefix = "dict", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DictAutoConfiguration {

    @Bean
    public DictModule dictModule(DictLoader dictLoader, JdbcTemplate jdbcTemplate) {
        DictCache.init(dictLoader, jdbcTemplate);  // 初始化DictCache
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new DictSerializer());
        return new DictModule(module);
    }

    @Bean
    public DictRefresher dictRefresher(DictProperties properties) {
        return new DictRefresher(properties);
    }
} 