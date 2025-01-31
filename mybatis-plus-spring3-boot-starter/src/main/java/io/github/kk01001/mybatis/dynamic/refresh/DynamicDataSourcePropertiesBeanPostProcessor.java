package io.github.kk01001.mybatis.dynamic.refresh;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author linshiqiang
 * @date 2025-01-31 13:13:00
 * @description
 */
public class DynamicDataSourcePropertiesBeanPostProcessor implements BeanPostProcessor {

    private final DynamicDataSourceRefresher dataSourceRefresher;

    public DynamicDataSourcePropertiesBeanPostProcessor(DynamicDataSourceRefresher dataSourceRefresher) {
        this.dataSourceRefresher = dataSourceRefresher;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DynamicDataSourceProperties properties) {
            dataSourceRefresher.refreshDataSourceIfNeeded(properties);
        }
        return bean;
    }
}
