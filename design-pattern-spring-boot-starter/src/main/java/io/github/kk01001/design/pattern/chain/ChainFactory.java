package io.github.kk01001.design.pattern.chain;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 责任链工厂，用于管理和执行责任链
 *              通过Spring容器自动收集被@ChainGroup注解标记的ChainHandler实现类
 *              按照分组和优先级排序，支持缓存和动态执行责任链
 */
public class ChainFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 责任链缓存，key为分组名称，value为该分组下的处理器列表
     */
    private final Map<String, List<ChainHandler<?, ?>>> chainCache = new ConcurrentHashMap<>();

    /**
     * 执行责任链
     * 
     * @param groupName 分组名称
     * @param context   责任链上下文
     * @param data      待处理的数据
     * @param <T>       数据类型
     * @param <R>       结果类型
     * @return 处理结果
     */
    @SuppressWarnings("unchecked")
    public <T, R> R execute(String groupName, ChainContext<T, R> context, T data) {
        List<ChainHandler<T, R>> handlers = (List<ChainHandler<T, R>>) (List<?>) getChainHandlers(groupName);
        R result = null;

        for (ChainHandler<T, R> handler : handlers) {
            result = handler.handle(context, data);
            if (!handler.shouldContinue(result)) {
                break;
            }
        }

        return result;
    }

    /**
     * 获取指定分组的处理器列表
     * 
     * @param groupName 分组名称
     * @return 处理器列表
     */
    private List<ChainHandler<?, ?>> getChainHandlers(String groupName) {
        return chainCache.computeIfAbsent(groupName, key -> {
            Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ChainGroup.class);
            List<ChainHandler<?, ?>> handlers = new ArrayList<>();

            beans.values().stream()
                    .filter(bean -> bean instanceof ChainHandler)
                    .filter(bean -> {
                        ChainGroup annotation = bean.getClass().getAnnotation(ChainGroup.class);
                        return annotation != null && annotation.value().equals(groupName);
                    })
                    .map(bean -> (ChainHandler<?, ?>) bean)
                    .sorted(Comparator.comparingInt(handler -> {
                        ChainGroup annotation = handler.getClass().getAnnotation(ChainGroup.class);
                        return annotation != null ? annotation.order() : handler.getOrder();
                    }))
                    .forEach(handlers::add);

            return handlers;
        });
    }

    /**
     * 清除责任链缓存
     * 
     * @param groupName 分组名称，如果为null则清除所有缓存
     */
    public void clearCache(String groupName) {
        if (groupName == null) {
            chainCache.clear();
        } else {
            chainCache.remove(groupName);
        }
    }
}