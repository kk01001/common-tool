package io.github.archer099.redisson.config;

import io.github.archer099.redisson.factory.RedissonClientFactory;
import io.github.archer099.redisson.factory.RedissonClientFactoryImpl;
import io.github.archer099.redisson.properties.MultiRedissonProperties;
import io.github.archer099.redisson.properties.RedissonInstanceProperties;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * @author archer099
 * @date 2026-01-15 10:00:00
 * @description RedissonClient Bean 注册器，将所有配置的实例注册为 Spring Bean
 */
public class RedissonClientBeanRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private static final Logger log = LoggerFactory.getLogger(RedissonClientBeanRegistrar.class);

    private Environment environment;
    private final RedissonClientFactory factory = new RedissonClientFactoryImpl();

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // Bean 定义注册阶段不做处理
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 绑定配置
        MultiRedissonProperties properties = Binder.get(environment)
                .bind("redisson.multi", MultiRedissonProperties.class)
                .orElse(null);

        if (properties == null || !properties.isEnabled()) {
            log.debug("Multi Redisson is not enabled");
            return;
        }

        Map<String, RedissonInstanceProperties> instances = properties.getInstances();
        if (instances == null || instances.isEmpty()) {
            log.warn("No Redisson instances configured");
            return;
        }

        String primaryName = properties.getPrimary();
        log.info("Registering {} RedissonClient beans, primary: {}", instances.size(), primaryName);

        // 注册每个实例为单独的 Bean
        for (Map.Entry<String, RedissonInstanceProperties> entry : instances.entrySet()) {
            String name = entry.getKey();
            RedissonInstanceProperties instanceProps = entry.getValue();

            try {
                // 创建 RedissonClient
                RedissonClient client = factory.create(name, instanceProps);

                // 注册为 Spring Bean
                String beanName = name + "RedissonClient";
                beanFactory.registerSingleton(beanName, client);

                log.info("Registered RedissonClient bean: {} ({})", beanName,
                        primaryName.equals(name) ? "primary" : "secondary");
            } catch (Exception e) {
                log.error("Failed to register RedissonClient bean: {}", name, e);
                throw new RuntimeException("Failed to register RedissonClient: " + name, e);
            }
        }
    }
}
