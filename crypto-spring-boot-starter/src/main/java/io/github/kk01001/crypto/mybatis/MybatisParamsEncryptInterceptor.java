package io.github.kk01001.crypto.mybatis;

import io.github.kk01001.crypto.ParamsCryptoProvider;
import io.github.kk01001.crypto.annotation.CryptoField;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.util.ReflectionUtils;

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