package io.github.archer099.localmessage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 本地消息自动配置类
 *
 * @author archer099
 */
@Slf4j
@AutoConfiguration
@EnableScheduling
@ComponentScan(basePackages = "io.github.archer099.localmessage")
@EnableConfigurationProperties(LocalMessageProperties.class)
@ConditionalOnProperty(prefix = "local-message", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LocalMessageAutoConfiguration {
    
    /**
     * 消息处理线程池（使用虚拟线程）
     */
    @Bean("messageProcessExecutor")
    @ConditionalOnMissingBean(name = "messageProcessExecutor")
    public ThreadPoolExecutor messageProcessExecutor(LocalMessageProperties properties) {
        LocalMessageProperties.ThreadPool threadPoolConfig = properties.getThreadPool();

        // 使用虚拟线程工厂
        ThreadFactory virtualThreadFactory = Thread.ofVirtual()
                .name(threadPoolConfig.getThreadNamePrefix(), 0)
                .factory();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threadPoolConfig.getQueueCapacity()),
                virtualThreadFactory,
                loadRejectedHandler(threadPoolConfig.getRejectedExecutionHandlerClass())
        );

        log.info("初始化本地消息处理虚拟线程池: corePoolSize={}, maximumPoolSize={}, queueCapacity={}",
                threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getQueueCapacity());

        return executor;
    }

    public static RejectedExecutionHandler loadRejectedHandler(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return (RejectedExecutionHandler) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("无法实例化拒绝策略: " + className, e);
        }
    }

}
