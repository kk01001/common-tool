package io.github.kk01001.crypto.mybatis;

import io.github.kk01001.crypto.ParamsCryptoProvider;
import io.github.kk01001.crypto.annotation.CryptoField;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
})
@RequiredArgsConstructor
public class MybatisParamsEncryptInterceptor implements Interceptor {

    private final ParamsCryptoProvider cryptoProvider;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        if (target instanceof Executor) {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args[1];

            if (parameter != null) {
                // 更新操作，处理字段加密
                processEncrypt(parameter);
            }

            return invocation.proceed();
        }
        return invocation.proceed();
    }

    private boolean isSelect(MappedStatement ms) {
        return ms.getSqlCommandType().name().equals("SELECT");
    }

    @SuppressWarnings("all")
    private void handleQueryParameterEncrypt(MappedStatement ms, Object parameter) {
        if (parameter == null) {
            return;
        }

        Class<?> parameterType = ms.getParameterMap().getType();
        BoundSql boundSql = ms.getBoundSql(parameter);
        Configuration configuration = ms.getConfiguration();

        if (parameter instanceof Map) {
            Map<String, Object> paramMap = (Map<String, Object>) parameter;

            // 处理参数映射
            for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                String property = parameterMapping.getProperty();
                Object value = getParameterValue(configuration, paramMap, property);

                if (value instanceof String) {
                    // 1. 检查参数类型中是否有加密注解
                    Field field = findFieldWithCryptoAnnotation(parameterType, property);
                    if (field != null) {
                        // 尝试解密，如果解密失败则说明是原文需要加密
                        try {
                            String decrypted = cryptoProvider.decrypt((String) value);
                            if (decrypted.equals(value)) {
                                // 原文，需要加密
                                String encrypted = cryptoProvider.encrypt((String) value);
                                setParameterValue(configuration, paramMap, property, encrypted);
                            }
                            // 已经是密文，不需要处理
                        } catch (Exception e) {
                            // 解密失败，说明是原文，需要加密
                            String encrypted = cryptoProvider.encrypt((String) value);
                            setParameterValue(configuration, paramMap, property, encrypted);
                        }
                        continue;
                    }

                    // 2. 检查参数值对象中是否有加密注解
                    Object paramValue = paramMap.get(property);
                    if (paramValue != null) {
                        field = findFieldWithCryptoAnnotation(paramValue.getClass(), property);
                        if (field != null) {
                            try {
                                String decrypted = cryptoProvider.decrypt((String) value);
                                if (decrypted.equals(value)) {
                                    String encrypted = cryptoProvider.encrypt((String) value);
                                    setParameterValue(configuration, paramMap, property, encrypted);
                                }
                            } catch (Exception e) {
                                String encrypted = cryptoProvider.encrypt((String) value);
                                setParameterValue(configuration, paramMap, property, encrypted);
                            }
                        }
                    }
                }
            }
        } else {
            // 处理单个参数对象
            processEncrypt(parameter);
        }
    }

    private Field findFieldWithCryptoAnnotation(Class<?> clazz, String fieldName) {
        if (clazz == null || Object.class.equals(clazz)) {
            return null;
        }

        try {
            Field field = clazz.getDeclaredField(getFieldName(fieldName));
            if (field.isAnnotationPresent(CryptoField.class)) {
                return field;
            }
        } catch (NoSuchFieldException e) {
            // 尝试在父类中查找
            return findFieldWithCryptoAnnotation(clazz.getSuperclass(), fieldName);
        }
        return null;
    }

    private String getFieldName(String property) {
        int dotIndex = property.lastIndexOf('.');
        return dotIndex > 0 ? property.substring(dotIndex + 1) : property;
    }

    private Object getParameterValue(Configuration configuration, Object parameterObject, String propertyName) {
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        return metaObject.getValue(propertyName);
    }

    private void setParameterValue(Configuration configuration, Object parameterObject, String propertyName, Object value) {
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        metaObject.setValue(propertyName, value);
    }

    private void processEncrypt(Object obj) {
        if (obj == null) {
            return;
        }

        ReflectionUtils.doWithFields(obj.getClass(), field -> {
            CryptoField annotation = field.getAnnotation(CryptoField.class);
            if (annotation != null && annotation.encrypt()) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value instanceof String) {
                    try {
                        String decrypted = cryptoProvider.decrypt((String) value);
                        if (decrypted.equals(value)) {
                            // 原文，需要加密
                            String encrypted = cryptoProvider.encrypt((String) value);
                            field.set(obj, encrypted);
                        }
                    } catch (Exception e) {
                        // 解密失败，说明是原文，需要加密
                        String encrypted = cryptoProvider.encrypt((String) value);
                        field.set(obj, encrypted);
                    }
                }
            }
        });
    }

    private Object handleResultDecrypt(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if (result instanceof Collection) {
            ((Collection<?>) result).forEach(this::processDecrypt);
        } else {
            processDecrypt(result);
        }
        return result;
    }

    private void processDecrypt(Object obj) {
        if (obj == null) {
            return;
        }

        ReflectionUtils.doWithFields(obj.getClass(), field -> {
            CryptoField annotation = field.getAnnotation(CryptoField.class);
            if (annotation != null && annotation.decrypt()) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value instanceof String) {
                    String decrypted = cryptoProvider.decrypt((String) value);
                    field.set(obj, decrypted);
                }
            }
        });
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
} 