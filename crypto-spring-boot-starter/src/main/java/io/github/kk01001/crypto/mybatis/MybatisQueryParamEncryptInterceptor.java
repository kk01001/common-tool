package io.github.kk01001.crypto.mybatis;

import io.github.kk01001.crypto.ParamsCryptoProvider;
import io.github.kk01001.crypto.annotation.CryptoField;
import io.github.kk01001.crypto.config.ParamsCryptoProperties;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.springframework.util.ReflectionUtils;

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
        if (Objects.isNull(parameterObject)) {
            return;
        }
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
        if (Objects.isNull(parameterObject)) {
            return;
        }
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