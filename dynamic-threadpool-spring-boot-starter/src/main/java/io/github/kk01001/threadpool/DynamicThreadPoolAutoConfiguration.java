package io.github.kk01001.threadpool;

import io.github.kk01001.threadpool.actuator.ThreadPoolActuatorEndpoint;
import io.github.kk01001.threadpool.alarm.ThreadPoolAlarmHandler;
import io.github.kk01001.threadpool.custom.config.DynamicThreadPoolProperties;
import io.github.kk01001.threadpool.custom.factory.ThreadPoolFactory;
import io.github.kk01001.threadpool.custom.handler.ThreadPoolRefreshHandler;
import io.github.kk01001.threadpool.custom.initializer.ThreadPoolAutoInitializer;
import io.github.kk01001.threadpool.custom.monitor.ThreadPoolMonitor;
import io.github.kk01001.threadpool.custom.processor.DynamicThreadPoolPostProcessor;
import io.github.kk01001.threadpool.registry.ThreadPoolRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 动态线程池自动配置类
 *
 * @author kk01001
 */
@Slf4j
@AutoConfiguration
@EnableScheduling
@ConditionalOnProperty(prefix = "dynamic-threadpool", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamicThreadPoolAutoConfiguration {

    @Bean("dynamicThreadPoolProperties")
    public DynamicThreadPoolProperties dynamicThreadPoolProperties() {
        return new DynamicThreadPoolProperties();
    }

    /**
     * 线程池注册中心
     */
    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolRegistry threadPoolRegistry() {
        log.info("Initializing ThreadPoolRegistry");
        return new ThreadPoolRegistry();
    }

    /**
     * 线程池工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolFactory threadPoolFactory(DynamicThreadPoolProperties properties) {
        log.info("Initializing ThreadPoolFactory");
        return new ThreadPoolFactory(properties);
    }

    /**
     * 线程池自动初始化器
     * 在应用启动时根据配置文件自动创建线程池
     */
    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolAutoInitializer threadPoolAutoInitializer(
            DynamicThreadPoolProperties properties,
            ThreadPoolFactory factory,
            ThreadPoolRegistry registry) {
        log.info("Initializing ThreadPoolAutoInitializer");
        return new ThreadPoolAutoInitializer(properties, factory, registry);
    }

    /**
     * BeanPostProcessor
     */
    @Bean
    @ConditionalOnMissingBean
    public DynamicThreadPoolPostProcessor dynamicThreadPoolPostProcessor(
            ThreadPoolRegistry registry,
            ThreadPoolFactory factory) {
        log.info("Initializing DynamicThreadPoolPostProcessor");
        return new DynamicThreadPoolPostProcessor(registry, factory);
    }

    /**
     * 配置刷新处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolRefreshHandler threadPoolRefreshHandler(
            ThreadPoolRegistry registry,
            DynamicThreadPoolProperties properties) {
        log.info("Initializing ThreadPoolRefreshHandler");
        return new ThreadPoolRefreshHandler(registry, properties);
    }

    /**
     * 线程池监控器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "dynamic-threadpool.monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(MeterRegistry.class)
    public ThreadPoolMonitor threadPoolMonitor(
            ThreadPoolRegistry registry,
            DynamicThreadPoolProperties properties,
            MeterRegistry meterRegistry) {
        log.info("Initializing ThreadPoolMonitor with Micrometer");
        return new ThreadPoolMonitor(registry, properties, meterRegistry);
    }

    /**
     * 线程池监控器（无 Micrometer）
     */
    @Bean
    @ConditionalOnMissingBean(MeterRegistry.class)
    @ConditionalOnProperty(prefix = "dynamic-threadpool.monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ThreadPoolMonitor threadPoolMonitorWithoutMicrometer(
            ThreadPoolRegistry registry,
            DynamicThreadPoolProperties properties) {
        log.info("Initializing ThreadPoolMonitor without Micrometer");
        return new ThreadPoolMonitor(registry, properties, null);
    }

    /**
     * 告警通知器 - 日志
     */
    @Bean
    @ConditionalOnMissingBean(name = "logAlarmNotifier")
    @ConditionalOnProperty(prefix = "dynamic-threadpool.alarm", name = "enabled", havingValue = "true", matchIfMissing = true)
    public io.github.kk01001.threadpool.alarm.notifier.LogAlarmNotifier logAlarmNotifier() {
        log.info("Initializing LogAlarmNotifier");
        return new io.github.kk01001.threadpool.alarm.notifier.LogAlarmNotifier();
    }

    /**
     * 告警通知器 - 企业微信
     */
    @Bean
    @ConditionalOnMissingBean(name = "weChatAlarmNotifier")
    @ConditionalOnProperty(prefix = "dynamic-threadpool.alarm", name = "enabled", havingValue = "true", matchIfMissing = true)
    public io.github.kk01001.threadpool.alarm.notifier.WeChatAlarmNotifier weChatAlarmNotifier(
            DynamicThreadPoolProperties properties) {
        log.info("Initializing WeChatAlarmNotifier");
        return new io.github.kk01001.threadpool.alarm.notifier.WeChatAlarmNotifier(properties.getAlarm());
    }

    /**
     * 告警通知器 - 钉钉
     */
    @Bean
    @ConditionalOnMissingBean(name = "dingTalkAlarmNotifier")
    @ConditionalOnProperty(prefix = "dynamic-threadpool.alarm", name = "enabled", havingValue = "true", matchIfMissing = true)
    public io.github.kk01001.threadpool.alarm.notifier.DingTalkAlarmNotifier dingTalkAlarmNotifier(
            DynamicThreadPoolProperties properties) {
        log.info("Initializing DingTalkAlarmNotifier");
        return new io.github.kk01001.threadpool.alarm.notifier.DingTalkAlarmNotifier(properties.getAlarm());
    }

    /**
     * 告警处理器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "dynamic-threadpool.alarm", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ThreadPoolAlarmHandler threadPoolAlarmHandler(
            ThreadPoolRegistry registry,
            DynamicThreadPoolProperties properties,
            java.util.List<io.github.kk01001.threadpool.alarm.notifier.AlarmNotifier> alarmNotifiers,
            @org.springframework.beans.factory.annotation.Value("${spring.application.name:unknown}") String applicationName) {
        log.info("Initializing ThreadPoolAlarmHandler");
        return new ThreadPoolAlarmHandler(registry, properties, alarmNotifiers, applicationName);
    }

    /**
     * 注册告警处理器到所有线程池
     */
    @Bean
    @ConditionalOnBean(ThreadPoolAlarmHandler.class)
    public ApplicationRunner alarmHandlerInjector(
            ThreadPoolRegistry registry,
            ThreadPoolAlarmHandler alarmHandler) {
        return args -> {
            // 将告警处理器注入到所有线程池包装器
            registry.getAllThreadPools().forEach(wrapper -> wrapper.setAlarmHandler(alarmHandler));
            log.info("AlarmHandler injected into {} thread pools", registry.size());
        };
    }

    /**
     * Actuator 端点
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "dynamic-threadpool.monitor", name = "enable-actuator", havingValue = "true", matchIfMissing = true)
    public ThreadPoolActuatorEndpoint threadPoolActuatorEndpoint(ThreadPoolRegistry registry) {
        log.info("Initializing ThreadPoolActuatorEndpoint");
        return new ThreadPoolActuatorEndpoint(registry);
    }

    /**
     * 应用启动时的初始化
     */
    @Bean
    public ApplicationRunner dynamicThreadPoolInitializer(
            ThreadPoolRegistry registry,
            DynamicThreadPoolProperties properties) {
        return args -> {
            log.info("Dynamic ThreadPool Starter initialized successfully");
            log.info("Total thread pools registered: {}", registry.size());
            log.info("Monitor enabled: {}", properties.getMonitor().getEnabled());
            log.info("Alarm enabled: {}", properties.getAlarm().getEnabled());
        };
    }
}
