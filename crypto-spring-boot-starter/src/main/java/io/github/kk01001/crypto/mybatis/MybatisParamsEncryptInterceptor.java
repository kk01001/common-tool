package io.github.kk01001.crypto.mybatis;

import io.github.kk01001.crypto.ParamsCryptoProvider;
import io.github.kk01001.crypto.annotation.CryptoField;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
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
            Object parameter = args[1];

            if (parameter != null) {
                // 更新操作，处理字段加密
                processEncrypt(parameter);
            }

            return invocation.proceed();
        }
        return invocation.proceed();
    }

    @SuppressWarnings("all")
    private void processEncrypt(Object parameterObject) {
        if (Objects.isNull(parameterObject)) {
            return;
        }
        Class<?> parameterObjectClass = parameterObject.getClass();
        // map
        if (parameterObject instanceof Map) {
            Map parameterObjectMap = (Map) parameterObject;
            if (parameterObjectMap.containsKey("param1")) {
                Object param1 = parameterObjectMap.get("param1");
                if (param1 instanceof ArrayList) {
                    ArrayList list = (ArrayList) param1;
                    for (Object elementObject : list) {
                        encrypt(elementObject);
                    }
                    return;
                }
                encrypt(param1);
                return;
            }

            // Mybatis Plus 批量修改修改
            if (parameterObjectMap.containsKey("et")) {
                Object et = parameterObjectMap.get("et");
                encrypt(et);
                return;
            }

            encrypt(parameterObject);
            return;
        }

        // TODO list

        // 对象
        encrypt(parameterObject);

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

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
} 