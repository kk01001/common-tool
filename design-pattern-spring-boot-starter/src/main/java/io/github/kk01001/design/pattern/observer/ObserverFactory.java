package io.github.kk01001.design.pattern.observer;

import io.github.kk01001.design.pattern.observer.annotation.Observer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 观察者工厂，用于管理观察者和主题之间的关系
 */
public class ObserverFactory implements ApplicationContextAware, InitializingBean {
    
    /**
     * 观察者缓存，key为主题名称，value为该主题下的所有观察者
     */
    private final Map<String, List<IObserver<?>>> observerCache = new ConcurrentHashMap<>();
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void afterPropertiesSet() {
        // 获取所有带有Observer注解的Bean
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Observer.class);
        
        // 按主题分组收集观察者
        beans.values().stream()
                .filter(bean -> bean instanceof IObserver<?>)
                .forEach(bean -> {
                    Observer annotation = AnnotationUtils.findAnnotation(bean.getClass(), Observer.class);
                    if (annotation == null) {
                        return;
                    }
                    
                    String topic = annotation.topic();
                    IObserver<?> observer = (IObserver<?>) bean;
                    
                    // 将观察者添加到对应的主题组
                    observerCache.computeIfAbsent(topic, k -> new ArrayList<>())
                            .add(observer);
                    
                    // 按优先级排序
                    observerCache.get(topic).sort(Comparator.comparingInt(IObserver::getOrder));
                });
    }
    
    /**
     * 获取指定主题下的所有观察者
     *
     * @param topic 主题名称
     * @param <T>   数据类型
     * @return 观察者列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<IObserver<T>> getObservers(String topic) {
        List<IObserver<?>> observers = observerCache.getOrDefault(topic, new ArrayList<>());
        return observers.stream()
                .map(observer -> (IObserver<T>) observer)
                .toList();
    }
    
    /**
     * 通知指定主题的所有观察者
     *
     * @param topic 主题名称
     * @param data  通知数据
     * @param <T>   数据类型
     */
    public <T> void notifyObservers(String topic, T data) {
        List<IObserver<T>> observers = getObservers(topic);
        observers.forEach(observer -> {
            try {
                observer.onUpdate(data);
            } catch (Exception e) {
                throw new RuntimeException("Observer " + observer.getName() + " update error", e);
            }
        });
    }
}