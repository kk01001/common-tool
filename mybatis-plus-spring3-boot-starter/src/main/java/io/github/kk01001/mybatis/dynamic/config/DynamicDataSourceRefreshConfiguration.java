package io.github.kk01001.mybatis.dynamic.config;

import com.baomidou.dynamic.datasource.creator.hikaricp.HikariDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import io.github.kk01001.mybatis.dynamic.refresh.DynamicDataSourcePropertiesBeanPostProcessor;
import io.github.kk01001.mybatis.dynamic.refresh.DynamicDataSourceRefresher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "spring.datasource.dynamic", name = "enabled", havingValue = "true")
public class DynamicDataSourceRefreshConfiguration {

    @Bean
    @ConditionalOnBean(DataSource.class)
    public DynamicDataSourceRefresher dataSourceRefresher(
            DataSource dataSource,
            DynamicDataSourceProperties properties,
            HikariDataSourceCreator dataSourceCreator) {
        return new DynamicDataSourceRefresher(dataSource, properties, dataSourceCreator);
    }

    @Bean
    public DynamicDataSourcePropertiesBeanPostProcessor dynamicDataSourcePropertiesBeanPostProcessor(
            DynamicDataSourceRefresher dynamicDataSourceRefresher) {
        return new DynamicDataSourcePropertiesBeanPostProcessor(dynamicDataSourceRefresher);
    }
} 