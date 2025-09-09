package io.github.kk01001.localmessage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本地消息自动配置类
 *
 * @author kk01001
 */
@Slf4j
@AutoConfiguration
@EnableScheduling
@ComponentScan(basePackages = "io.github.kk01001.localmessage")
@EnableConfigurationProperties(LocalMessageProperties.class)
@ConditionalOnProperty(prefix = "local-message", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LocalMessageAutoConfiguration {
    
    /**
     * 消息处理线程池
     */
    @Bean("messageProcessExecutor")
    @ConditionalOnMissingBean(name = "messageProcessExecutor")
    public ThreadPoolExecutor messageProcessExecutor(LocalMessageProperties properties) {
        LocalMessageProperties.ThreadPool threadPoolConfig = properties.getThreadPool();
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threadPoolConfig.getQueueCapacity()),
                new LocalMessageThreadFactory(threadPoolConfig.getThreadNamePrefix()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        log.info("初始化本地消息处理线程池: corePoolSize={}, maximumPoolSize={}, queueCapacity={}", 
                threadPoolConfig.getCorePoolSize(), 
                threadPoolConfig.getMaximumPoolSize(), 
                threadPoolConfig.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * 自定义线程工厂
     */
    private static class LocalMessageThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        LocalMessageThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
