package io.github.kk01001.resilience4j.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.kk01001.resilience4j.enums.FallbackStrategy;
import io.github.kk01001.resilience4j.exception.ResilienceException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 降级处理器
 *
 * @author kk01001
 */
public class FallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(FallbackHandler.class);

    public static Object handle(ProceedingJoinPoint joinPoint, FallbackStrategy strategy, 
                                String fallbackMethod, String fallbackValue, Throwable throwable) {
        switch (strategy) {
            case METHOD:
                return invokeFallbackMethod(joinPoint, fallbackMethod, throwable);
            case DEFAULT_VALUE:
                return getDefaultValue(joinPoint, fallbackValue);
            case NULL:
                return null;
            case EXCEPTION:
            default:
                throw new ResilienceException("Resilience protection triggered", throwable);
        }
    }

    private static Object invokeFallbackMethod(ProceedingJoinPoint joinPoint, String fallbackMethodName, Throwable throwable) {
        if (StrUtil.isBlank(fallbackMethodName)) {
            throw new ResilienceException("Fallback method name is not specified");
        }

        try {
            Object target = joinPoint.getTarget();
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] args = joinPoint.getArgs();

            Method fallbackMethod;
            try {
                Class<?>[] fallbackParamTypes = new Class<?>[parameterTypes.length + 1];
                System.arraycopy(parameterTypes, 0, fallbackParamTypes, 0, parameterTypes.length);
                fallbackParamTypes[parameterTypes.length] = Throwable.class;
                fallbackMethod = target.getClass().getDeclaredMethod(fallbackMethodName, fallbackParamTypes);
                
                Object[] fallbackArgs = new Object[args.length + 1];
                System.arraycopy(args, 0, fallbackArgs, 0, args.length);
                fallbackArgs[args.length] = throwable;
                
                fallbackMethod.setAccessible(true);
                return fallbackMethod.invoke(target, fallbackArgs);
            } catch (NoSuchMethodException e) {
                fallbackMethod = target.getClass().getDeclaredMethod(fallbackMethodName, parameterTypes);
                fallbackMethod.setAccessible(true);
                return fallbackMethod.invoke(target, args);
            }
        } catch (Exception e) {
            log.error("Failed to invoke fallback method: {}", fallbackMethodName, e);
            throw new ResilienceException("Failed to invoke fallback method: " + fallbackMethodName, e);
        }
    }

    private static Object getDefaultValue(ProceedingJoinPoint joinPoint, String fallbackValue) {
        if (StrUtil.isBlank(fallbackValue)) {
            return null;
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> returnType = method.getReturnType();

        try {
            if (returnType == String.class) {
                return fallbackValue;
            } else if (returnType == Integer.class || returnType == int.class) {
                return Integer.parseInt(fallbackValue);
            } else if (returnType == Long.class || returnType == long.class) {
                return Long.parseLong(fallbackValue);
            } else if (returnType == Double.class || returnType == double.class) {
                return Double.parseDouble(fallbackValue);
            } else if (returnType == Float.class || returnType == float.class) {
                return Float.parseFloat(fallbackValue);
            } else if (returnType == Boolean.class || returnType == boolean.class) {
                return Boolean.parseBoolean(fallbackValue);
            } else {
                return JSONUtil.toBean(fallbackValue, returnType);
            }
        } catch (Exception e) {
            log.warn("Failed to parse fallback value: {}, return null", fallbackValue, e);
            return null;
        }
    }
}
