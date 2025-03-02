package io.github.kk01001.strategy;

import io.github.kk01001.strategy.annotation.Strategy;
import io.github.kk01001.strategy.exception.StrategyException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StrategyFactory implements ApplicationContextAware {
    
    /**
     * 策略映射
     * key: 策略枚举类
     * value: 具体策略实现映射(key: 策略类型, value: 策略实现类)
     */
    private static final Map<Class<? extends Enum<?>>, Map<String, IStrategy<?, ?>>> STRATEGY_MAP = new ConcurrentHashMap<>();
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取所有策略实现类
        Map<String, Object> strategyBeans = applicationContext.getBeansWithAnnotation(Strategy.class);
        
        // 遍历策略实现类
        strategyBeans.forEach((name, bean) -> {
            if (!(bean instanceof IStrategy)) {
                return;
            }
            
            // 获取策略注解
            Strategy strategy = AnnotationUtils.findAnnotation(bean.getClass(), Strategy.class);
            if (strategy == null) {
                return;
            }
            
            // 获取策略类型
            Class<? extends Enum<?>> strategyEnum = strategy.strategyEnum();
            String strategyType = strategy.strategyType();
            
            // 将策略实现类放入映射
            STRATEGY_MAP.computeIfAbsent(strategyEnum, k -> new HashMap<>())
                    .put(strategyType, (IStrategy<?, ?>) bean);
        });
    }
    
    /**
     * 获取策略实现类
     *
     * @param strategyEnum 策略枚举类
     * @param strategyType 策略类型
     * @return 策略实现类
     */
    @SuppressWarnings("unchecked")
    public <T, R> IStrategy<T, R> getStrategy(Class<? extends Enum<?>> strategyEnum, String strategyType) {
        Map<String, IStrategy<?, ?>> strategyMap = STRATEGY_MAP.get(strategyEnum);
        if (strategyMap == null) {
            throw new StrategyException("策略枚举类型未找到", strategyType, "STRATEGY_ENUM_NOT_FOUND");
        }
        
        IStrategy<?, ?> strategy = strategyMap.get(strategyType);
        if (strategy == null) {
            throw new StrategyException("未找到对应的策略实现", strategyType, "STRATEGY_NOT_FOUND");
        }
        
        return (IStrategy<T, R>) strategy;
    }
    
    /**
     * 执行策略
     *
     * @param strategyEnum 策略枚举类
     * @param strategyType 策略类型
     * @param param       策略参数
     * @return 策略执行结果
     */
    public <T, R> R execute(Class<? extends Enum<?>> strategyEnum, String strategyType, T param) {
        try {
            IStrategy<T, R> strategy = getStrategy(strategyEnum, strategyType);
            return strategy.execute(param);
        } catch (StrategyException e) {
            throw e;
        } catch (Exception e) {
            throw new StrategyException("策略执行失败", strategyType, "STRATEGY_EXECUTE_ERROR", e);
        }
    }
}