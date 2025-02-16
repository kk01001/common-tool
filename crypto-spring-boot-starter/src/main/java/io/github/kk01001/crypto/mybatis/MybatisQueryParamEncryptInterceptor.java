package io.github.kk01001.crypto.mybatis;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * mybatis 查询条件加密 指定字段
 */
@Intercepts({
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class})
})
@RequiredArgsConstructor
public class MybatisQueryParamEncryptInterceptor implements Interceptor {

    private final static Pattern PATTERN = Pattern.compile("(\\w+)\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(\\w+)\\}");

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

        // 参数是对象
        if (isEntity(parameterObject)) {
            encrypt(parameterObject);
            return;
        }


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

                    if (isLambdaQueryWrapper(value, mybatisCryptoColumn)) {
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

    /**
     * Mybatis Plus 查询
     * .eq
     */
    private boolean isLambdaQueryWrapper(Object parameterObject, List<String> mybatisCryptoColumn) {
        if (Objects.isNull(parameterObject)) {
            return false;
        }
        if (parameterObject instanceof LambdaQueryWrapper<?> wrapper) {
            Map<String, Object> paramNameValuePairs = wrapper.getParamNameValuePairs();
            if (CollUtil.isEmpty(paramNameValuePairs)) {
                Object wrapperEntity = wrapper.getEntity();
                if (Objects.nonNull(wrapperEntity)) {
                    encrypt(wrapperEntity);
                }
                return true;
            }
            String sqlSegment = wrapper.getExpression().getSqlSegment();
            Map<String, String> params = getParams(sqlSegment);
            if (CollUtil.isEmpty(params)) {
                return true;
            }

            params.forEach((paramKey, columnName) -> {
                if (mybatisCryptoColumn.contains(columnName)) {
                    String value = paramNameValuePairs.get(paramKey).toString();
                    paramNameValuePairs.put(paramKey, cryptoProvider.encrypt(value));
                }
            });
            return true;
        }
        return false;
    }

    private Map<String, String> getParams(String sqlSegment) {
        // (name = #{ew.paramNameValuePairs.MPGENVAL1} AND email = #{ew.paramNameValuePairs.MPGENVAL2})
        // 2. 创建匹配器
        Matcher matcher = PATTERN.matcher(sqlSegment);

        // 3. 存放结果的 Map，key: MPGENVAL1, value: name
        Map<String, String> resultMap = new LinkedHashMap<>();

        // 4. 遍历所有匹配
        while (matcher.find()) {
            // group(1) = 列名，例如 name / email
            // group(2) = 占位符，例如 MPGENVAL1 / MPGENVAL2
            String columnName = matcher.group(1);
            String paramKey = matcher.group(2);

            // 存入 map: MPGENVAL1 -> name
            resultMap.put(paramKey, columnName);
        }
        return resultMap;
    }

    @SuppressWarnings("all")
    private boolean isEntity(Object value) {
        if (Objects.isNull(value)) {
            return false;
        }
        return switch (value) {
            case String s -> false;
            case Long l -> false;
            case Integer i -> false;
            case Map map -> false;
            case List list -> false;
            default -> true;
        };
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