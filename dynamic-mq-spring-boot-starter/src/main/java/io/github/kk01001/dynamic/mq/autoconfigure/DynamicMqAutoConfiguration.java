package io.github.kk01001.dynamic.mq.autoconfigure;

import io.github.kk01001.dynamic.mq.config.DynamicMqProperties;
import io.github.kk01001.dynamic.mq.consumer.ConsumerRegistry;
import io.github.kk01001.dynamic.mq.factory.MqConsumerFactory;
import io.github.kk01001.dynamic.mq.factory.MqProducerFactory;
import io.github.kk01001.dynamic.mq.manager.DynamicMqConsumerManager;
import io.github.kk01001.dynamic.mq.manager.DynamicMqManager;
import io.github.kk01001.dynamic.mq.manager.DynamicMqProducerManager;
import io.github.kk01001.dynamic.mq.processor.MqListenerAnnotationProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author linshiqiang
 * @date 2025-06-21 20:48:56
 * @description 动态MQ自动配置类
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DynamicMqProperties.class)
@ConditionalOnProperty(prefix = "dynamic.mq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamicMqAutoConfiguration {
    
    @Bean
    public MqProducerFactory mqProducerFactory(DynamicMqProperties properties) {
        log.info("创建MQ生产者工厂");
        return new MqProducerFactory(properties);
    }

    @Bean
    public MqConsumerFactory mqConsumerFactory(DynamicMqProperties properties) {
        log.info("创建MQ消费者工厂");
        return new MqConsumerFactory(properties);
    }

    @Bean
    public ConsumerRegistry consumerRegistry(DynamicMqProperties properties) {
        log.info("创建消费者注册中心");
        return new ConsumerRegistry(properties);
    }

    @Bean
    public MqListenerAnnotationProcessor mqListenerAnnotationProcessor(ConsumerRegistry consumerRegistry) {
        log.info("创建MQ监听器注解处理器");
        return new MqListenerAnnotationProcessor(consumerRegistry);
    }

    @Bean
    public DynamicMqProducerManager dynamicMqProducerManager(DynamicMqProperties properties,
                                                            MqProducerFactory producerFactory) {
        log.info("创建动态MQ生产者管理器");
        return new DynamicMqProducerManager(properties, producerFactory);
    }

    @Bean
    public DynamicMqConsumerManager dynamicMqConsumerManager(DynamicMqProperties properties,
                                                            MqConsumerFactory consumerFactory,
                                                            ConsumerRegistry consumerRegistry) {
        log.info("创建动态MQ消费者管理器");
        return new DynamicMqConsumerManager(properties, consumerFactory, consumerRegistry);
    }

    @Bean
    public DynamicMqManager dynamicMqManager(DynamicMqProperties properties,
                                           DynamicMqProducerManager producerManager,
                                           DynamicMqConsumerManager consumerManager) {
        log.info("创建动态MQ管理器");
        return new DynamicMqManager(properties, producerManager, consumerManager);
    }
}
