package io.github.kk01001.crypto.mybatis;

import io.github.kk01001.crypto.ParamsCryptoProvider;
import io.github.kk01001.crypto.annotation.CryptoField;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.util.ReflectionUtils;

import java.sql.Statement;
import java.util.Collection;
import java.util.Properties;

/**
 * mybatis 查询结果解密
 * 返回值需要是实体类 并使用注解
 */
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
@RequiredArgsConstructor
public class MybatisQueryResultDecryptInterceptor implements Interceptor {

    private final ParamsCryptoProvider cryptoProvider;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        if (target instanceof ResultSetHandler) {
            return handleResultDecrypt(invocation);
        }
        return invocation.proceed();
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