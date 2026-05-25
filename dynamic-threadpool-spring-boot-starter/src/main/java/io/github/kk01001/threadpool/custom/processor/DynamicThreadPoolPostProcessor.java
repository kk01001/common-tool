package io.github.archer099.threadpool.custom.processor;

import io.github.archer099.threadpool.custom.annotation.DynamicThreadPool;
import io.github.archer099.threadpool.custom.factory.ThreadPoolFactory;
import io.github.archer099.threadpool.custom.model.ThreadPoolConfig;
import io.github.archer099.threadpool.custom.wrapper.DynamicThreadPoolWrapper;
import io.github.archer099.threadpool.registry.ThreadPoolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * 动态线程池 BeanPostProcessor
 * 扫描并处理 @DynamicThreadPool 注解
 *
 * @author archer099
 */
@Slf4j
public class DynamicThreadPoolPostProcessor implements BeanPostProcessor {

    private final ThreadPoolRegistry registry;
    private final ThreadPoolFactory factory;

    public DynamicThreadPoolPostProcessor(ThreadPoolRegistry registry, ThreadPoolFactory factory) {
        this.registry = registry;
        this.factory = factory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 检查类级别的注解
        Class<?> clazz = bean.getClass();
        DynamicThreadPool classAnnotation = AnnotationUtils.findAnnotation(clazz, DynamicThreadPool.class);

        if (classAnnotation != null) {
            processAnnotation(classAnnotation, beanName);
        }

        // 检查方法级别的注解
        for (Method method : clazz.getDeclaredMethods()) {
            DynamicThreadPool methodAnnotation = AnnotationUtils.findAnnotation(method, DynamicThreadPool.class);
            if (methodAnnotation != null) {
                processAnnotation(methodAnnotation, beanName);
            }
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 如果 bean 是 Executor 且有注解，进行处理
        if (bean instanceof Executor) {
            Class<?> clazz = bean.getClass();
            DynamicThreadPool annotation = AnnotationUtils.findAnnotation(clazz, DynamicThreadPool.class);

            if (annotation != null && bean instanceof java.util.concurrent.ThreadPoolExecutor) {
                // 如果已经是 ThreadPoolExecutor，包装它
                String poolName = annotation.poolName();
                log.info("Found ThreadPoolExecutor bean [{}] with @DynamicThreadPool annotation", poolName);
                // 注意：这里不再创建新的线程池，而是包装现有的
                // 实际使用时，建议通过工厂方法创建
            }
        }

        return bean;
    }

    /**
     * 处理注解
     */
    private void processAnnotation(DynamicThreadPool annotation, String beanName) {
        String poolName = annotation.poolName();

        // 检查线程池是否已存在
        if (registry.getThreadPool(poolName) != null) {
            log.warn("Thread pool [{}] already exists, skipping annotation processing", poolName);
            return;
        }

        // 构建配置
        ThreadPoolConfig.ThreadPoolConfigBuilder configBuilder = ThreadPoolConfig.builder()
                .poolName(poolName);

        if (annotation.corePoolSize() > 0) {
            configBuilder.corePoolSize(annotation.corePoolSize());
        }

        if (annotation.maxPoolSize() > 0) {
            configBuilder.maxPoolSize(annotation.maxPoolSize());
        }

        if (annotation.queueCapacity() > 0) {
            configBuilder.queueCapacity(annotation.queueCapacity());
        }

        if (annotation.keepAliveSeconds() > 0) {
            configBuilder.keepAliveTime(Duration.ofSeconds(annotation.keepAliveSeconds()));
        }

        if (!annotation.threadNamePrefix().isEmpty()) {
            configBuilder.threadNamePrefix(annotation.threadNamePrefix());
        }

        ThreadPoolConfig customConfig = configBuilder.build();

        // 创建线程池
        DynamicThreadPoolWrapper wrapper = factory.createThreadPool(poolName, customConfig);

        // 注册线程池
        registry.register(poolName, wrapper);

        log.info("Thread pool [{}] created from @DynamicThreadPool annotation on bean [{}]",
                poolName, beanName);
    }
}
