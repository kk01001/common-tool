package io.github.kk01001.design.pattern.responsibility;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 责任链工厂，用于管理和执行责任链
 * 通过Spring容器自动收集被@ChainGroup注解标记的ChainHandler实现类
 * 按照分组和优先级排序，支持缓存和动态执行责任链
 * 实现InitializingBean接口，在Spring容器启动时预加载所有处理器
 */
public class ResponsibilityChainFactory implements ApplicationContextAware, InitializingBean {

    /**
     * 责任链缓存，key为分组名称，value为该分组下的处理器列表
     */
    private final Map<String, List<ResponsibilityChainHandler<?, ?>>> chainCache = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Spring容器启动时自动执行，预加载所有责任链处理器
     */
    @Override
    public void afterPropertiesSet() {
        // 获取所有带有ResponsibilityChain注解的Bean
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ResponsibilityChain.class);

        // 按分组预加载所有处理器
        Map<String, List<ResponsibilityChainHandler<?, ?>>> groupHandlers = new HashMap<>();

        // 收集所有处理器并按分组归类
        beans.values().stream()
                .filter(bean -> bean instanceof ResponsibilityChainHandler)
                .forEach(bean -> {
                    ResponsibilityChain annotation = bean.getClass().getAnnotation(ResponsibilityChain.class);
                    if (annotation != null) {
                        String groupName = annotation.value();
                        groupHandlers.computeIfAbsent(groupName, k -> new ArrayList<>())
                                .add((ResponsibilityChainHandler<?, ?>) bean);
                    }
                });

        // 对每个分组的处理器进行排序并缓存
        groupHandlers.forEach((groupName, handlers) -> {
            handlers.sort(Comparator.comparingInt(handler -> {
                ResponsibilityChain annotation = handler.getClass().getAnnotation(ResponsibilityChain.class);
                return annotation != null ? annotation.order() : handler.getOrder();
            }));
            chainCache.put(groupName, handlers);
        });
    }

    /**
     * 执行责任链
     *
     * @param groupName 分组名称
     * @param context   责任链上下文
     * @param <T>       数据类型
     * @param <R>       结果类型
     * @return 处理结果
     */
    @SuppressWarnings("unchecked")
    public <T, R> R execute(String groupName, ResponsibilityChainContext<T, R> context) throws Exception {
        List<ResponsibilityChainHandler<T, R>> handlers = (List<ResponsibilityChainHandler<T, R>>) (List<?>) getChainHandlers(groupName);
        R result = null;

        for (ResponsibilityChainHandler<T, R> handler : handlers) {
            result = handler.handle(context);
            if (handler.shouldTerminated(context)) {
                break;
            }
        }

        return result;
    }

    /**
     * 执行责任链 无返回值
     *
     * @param groupName 分组名称
     * @param context   责任链上下文
     * @param <T>       数据类型
     * @param <R>       结果类型
     */
    @SuppressWarnings("unchecked")
    public <T, R> void executeVoid(String groupName, ResponsibilityChainContext<T, R> context) throws Exception {
        List<ResponsibilityChainHandler<T, R>> handlers = (List<ResponsibilityChainHandler<T, R>>) (List<?>) getChainHandlers(groupName);
        for (ResponsibilityChainHandler<T, R> handler : handlers) {
            handler.handleVoid(context);
            if (handler.shouldTerminated(context)) {
                break;
            }
        }

    }

    /**
     * 获取指定分组的处理器列表
     *
     * @param groupName 分组名称
     * @return 处理器列表
     */
    private List<ResponsibilityChainHandler<?, ?>> getChainHandlers(String groupName) {
        return chainCache.get(groupName);
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

    /**
     * 动态注册处理器
     *
     * @param groupName 分组名称
     * @param handler   处理器实例
     * @param <T>       数据类型
     * @param <R>       结果类型
     */
    public <T, R> void registerHandler(String groupName, ResponsibilityChainHandler<T, R> handler) {
        List<ResponsibilityChainHandler<?, ?>> handlers = chainCache.computeIfAbsent(groupName, k -> new ArrayList<>());
        handlers.add(handler);
        // 重新排序
        handlers.sort(Comparator.comparingInt(h -> {
            ResponsibilityChain annotation = h.getClass().getAnnotation(ResponsibilityChain.class);
            return annotation != null ? annotation.order() : h.getOrder();
        }));
    }

    /**
     * 移除处理器
     *
     * @param groupName    分组名称
     * @param handlerClass 处理器类
     */
    public void removeHandler(String groupName, Class<? extends ResponsibilityChainHandler<?, ?>> handlerClass) {
        List<ResponsibilityChainHandler<?, ?>> handlers = chainCache.get(groupName);
        if (handlers != null) {
            handlers.removeIf(handler -> handler.getClass().equals(handlerClass));
        }
    }
}