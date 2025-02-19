package io.github.kk01001.disruptor.processor;

import com.lmax.disruptor.dsl.Disruptor;
import io.github.kk01001.disruptor.annotation.DisruptorListener;
import io.github.kk01001.disruptor.event.DisruptorEvent;
import io.github.kk01001.disruptor.factory.DisruptorEventFactory;
import io.github.kk01001.disruptor.handler.DisruptorHandler;
import io.github.kk01001.disruptor.template.DisruptorTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadFactory;

/**
 * @author kk01001
 * @date 2025-02-19 10:15:47
 * @description Disruptor监听器处理器，用于处理@DisruptorListener注解
 */
@Slf4j
public class DisruptorListenerProcessor implements BeanPostProcessor {

    private final DisruptorTemplate disruptorTemplate;

    public DisruptorListenerProcessor(DisruptorTemplate disruptorTemplate) {
        this.disruptorTemplate = disruptorTemplate;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        ReflectionUtils.doWithMethods(targetClass, method -> {
            DisruptorListener listener = AnnotationUtils.findAnnotation(method, DisruptorListener.class);
            if (listener != null) {
                processDisruptorListener(bean, method, listener);
            }
        });
        return bean;
    }

    private void processDisruptorListener(Object bean, Method method, DisruptorListener listener) {
        String queueName = listener.value();
        ThreadFactory threadFactory = listener.virtualThread()
                ? Thread.ofVirtual().name("Disruptor-Virtual-Thread").factory()
                : Thread.ofPlatform().name("Disruptor-Platform-Thread").factory();

        DisruptorEventFactory<Object> factory = new DisruptorEventFactory<>();
        Disruptor<DisruptorEvent<Object>> disruptor = new Disruptor<>(
                factory,
                listener.bufferSize(),
                threadFactory,
                listener.producerType(),
                listener.waitStrategy().create()
        );

        // 创建自定义Handler来调用被注解的方法
        DisruptorHandler<Object> handler = new DisruptorHandler<>() {
            @Override
            public void onEvent(DisruptorEvent<Object> event, long sequence, boolean endOfBatch) {
                try {
                    method.invoke(bean, event.getData());
                } catch (Exception e) {
                    log.error("Error processing disruptor event", e);
                }
            }
        };

        // 配置多个消费者
        if (listener.threads() > 1) {
            DisruptorHandler<Object>[] handlers = new DisruptorHandler[listener.threads()];
            for (int i = 0; i < listener.threads(); i++) {
                handlers[i] = handler;
            }
            disruptor.handleEventsWith(handlers);
        } else {
            disruptor.handleEventsWith(handler);
        }

        disruptor.start();
        disruptorTemplate.registerDisruptor(queueName, disruptor);
        log.info("Registered DisruptorListener for queue: {}, threads: {}, virtualThread: {}",
                queueName, listener.threads(), listener.virtualThread());
    }
}