package io.github.kk01001.crypto.mybatis;

import cn.hutool.core.collection.CollUtil;
import io.github.kk01001.crypto.ParamsCryptoProvider;
import io.github.kk01001.crypto.annotation.CryptoField;
import io.github.kk01001.crypto.config.ParamsCryptoProperties;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * mybatis 查询条件加密 指定字段
 */
@Intercepts({
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class})
})
@RequiredArgsConstructor
public class MybatisQueryParamEncryptInterceptor implements Interceptor {

    private final ParamsCryptoProvider cryptoProvider;

    private final ParamsCryptoProperties paramsCryptoProperties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        if (target instanceof ParameterHandler parameterHandler) {
            processEncrypt(parameterHandler);
        }
        return invocation.proceed();
    }

    @SuppressWarnings("all")
    private void processEncrypt(ParameterHandler parameterHandler) {
        Object parameterObject = parameterHandler.getParameterObject();
        // 获取ParameterHandler的属性
        MetaObject metaObject = MetaObject.forObject(parameterHandler,
                SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                new DefaultReflectorFactory());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
        if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return;
        }
        BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
        Configuration configuration = mappedStatement.getConfiguration();

        // 参数是对象map
        if (parameterObject instanceof Map) {
            Map<?, ?> parameterObjectMap = (Map) parameterObject;
            List<String> mybatisCryptoColumn = paramsCryptoProperties.getMybatisCryptoColumn();

            for (Map.Entry<?, ?> entry : parameterObjectMap.entrySet()) {
                String key = (String) entry.getKey();
                Object value = entry.getValue();
                if (mybatisCryptoColumn.contains(key)) {
                    if (value instanceof String) {
                        setParameterValue(configuration, parameterObject, key, cryptoProvider.encrypt((String) value));
                        continue;
                    }
                }

                if ("param1".equals(key)) {
                    Map map = (Map) parameterObject;
                    Object param1 = map.get("param1");
                    if (param1 instanceof ArrayList) {
                        ArrayList list = (ArrayList) param1;
                        for (Object elementObject : list) {
                            encrypt(elementObject);
                        }
                        return;
                    }

                    if (isEntity(value)) {
                        encrypt(param1);
                        return;
                    }
                }
            }
        }

        // TODO 参数是list
    }

    private boolean isEntity(Object value) {
        if (value instanceof String) {
            return false;
        }
        if (value instanceof Long) {
            return false;
        }
        if (value instanceof Integer) {
            return false;
        }
        return true;
    }

    private void encrypt(Object parameterObject) {
        ReflectionUtils.doWithFields(parameterObject.getClass(), field -> {
            CryptoField annotation = field.getAnnotation(CryptoField.class);
            if (annotation != null && annotation.encrypt()) {
                field.setAccessible(true);
                Object value = field.get(parameterObject);
                if (Objects.nonNull(value)) {
                    if (value instanceof String) {
                        String encrypted = cryptoProvider.encrypt((String) value);
                        field.set(parameterObject, encrypted);
                    }
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleParameterEncrypt(ParameterHandler parameterHandler) throws Exception {

        List<String> mybatisCryptoColumn = paramsCryptoProperties.getMybatisCryptoColumn();
        if (CollUtil.isEmpty(mybatisCryptoColumn)) {
            return;
        }

        // 获取ParameterHandler的属性
        MetaObject metaObject = MetaObject.forObject(parameterHandler,
                SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                new DefaultReflectorFactory());

        // 获取参数对象和MappedStatement
        Object parameterObject = parameterHandler.getParameterObject();
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
        if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return;
        }
        BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
        Configuration configuration = mappedStatement.getConfiguration();

        // 处理参数映射
        for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
            String property = parameterMapping.getProperty();

            if (!mybatisCryptoColumn.contains(property)) {
                continue;
            }

            Object value = null;

            // 1. 处理Map类型参数
            if (parameterObject instanceof Map) {
                Map<String, Object> paramMap = (Map<String, Object>) parameterObject;
                value = getParameterValue(configuration, paramMap, property);
            }
            // 2. 处理普通对象参数
            else if (parameterObject != null) {
                value = getParameterValue(configuration, parameterObject, property);
            }

            // 处理字符串类型的值
            if (value instanceof String) {
                String encryptedValue = checkAndEncryptParameter(
                        mappedStatement.getParameterMap().getType(),
                        property,
                        (String) value,
                        parameterObject
                );

                if (!value.equals(encryptedValue)) {
                    if (parameterObject instanceof Map) {
                        setParameterValue(configuration, parameterObject, property, encryptedValue);
                    } else {
                        // 处理普通对象参数
                        String fieldName = getFieldName(property);
                        Field field = findFieldWithCryptoAnnotation(parameterObject.getClass(), fieldName);
                        if (field != null) {
                            field.setAccessible(true);
                            field.set(parameterObject, encryptedValue);
                        }
                    }
                }
            }
        }
    }

    private String checkAndEncryptParameter(Class<?> parameterType, String property, String value, Object parameterObject) {
        String fieldName = getFieldName(property);

        // 检查是否在配置的加密字段列表中
        List<String> mybatisCryptoColumn = paramsCryptoProperties.getMybatisCryptoColumn();
        if (mybatisCryptoColumn.contains(fieldName)) {
            return cryptoProvider.encrypt(value);
        }

        return value;
    }

    private String getFieldName(String property) {
        int dotIndex = property.lastIndexOf('.');
        return dotIndex > 0 ? property.substring(dotIndex + 1) : property;
    }

    private Field findFieldWithCryptoAnnotation(Class<?> clazz, String fieldName) {
        if (clazz == null || Object.class.equals(clazz)) {
            return null;
        }

        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field.isAnnotationPresent(CryptoField.class)) {
                return field;
            }
        } catch (NoSuchFieldException e) {
            return findFieldWithCryptoAnnotation(clazz.getSuperclass(), fieldName);
        }
        return null;
    }

    private Object getParameterValue(Configuration configuration, Object parameterObject, String propertyName) {
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        return metaObject.getValue(propertyName);
    }

    private void setParameterValue(Configuration configuration, Object parameterObject, String propertyName, Object value) {
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        metaObject.setValue(propertyName, value);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}