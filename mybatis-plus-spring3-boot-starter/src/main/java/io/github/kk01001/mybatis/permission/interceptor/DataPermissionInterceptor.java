package io.github.kk01001.mybatis.permission.interceptor;

import io.github.kk01001.mybatis.permission.DataPermissionProperties;
import io.github.kk01001.mybatis.permission.annotations.DataColumn;
import io.github.kk01001.mybatis.permission.annotations.DataPermission;
import io.github.kk01001.mybatis.permission.handler.DataPermissionHandlerFactory;
import io.github.kk01001.mybatis.permission.handler.DataPermissionType;
import io.github.kk01001.mybatis.permission.service.UserPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 数据权限SQL拦截器，拦截查询操作，动态注入数据权限条件
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
@RequiredArgsConstructor
public class DataPermissionInterceptor implements Interceptor {

    /**
     * 数据权限配置属性
     */
    private final DataPermissionProperties properties;

    /**
     * 数据权限处理器工厂
     */
    private final DataPermissionHandlerFactory handlerFactory;

    /**
     * 用户权限服务
     */
    private final UserPermissionService userPermissionService;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];

        if (mappedStatement.getSqlCommandType() != SqlCommandType.SELECT) {
            return invocation.proceed();
        }

        DataPermission dataPermission = getDataScopeAnnotation(mappedStatement);
        if (dataPermission == null || dataPermission.ignore()) {
            return invocation.proceed();
        }

        if (!StringUtils.hasLength(dataPermission.type())) {
            return invocation.proceed();
        }

        if (userPermissionService.isAdmin()) {
            return invocation.proceed();
        }

        Object parameter = args[1];
        BoundSql boundSql;
        if (args.length == 6) {
            boundSql = (BoundSql) args[5];
        } else {
            boundSql = mappedStatement.getBoundSql(parameter);
        }

        String originalSql = boundSql.getSql();

        if (properties.isSqlLog()) {
            log.debug("数据权限处理前SQL: {}", originalSql);
        }

        String modifiedSql = processSql(originalSql, dataPermission);

        if (properties.isSqlLog()) {
            log.debug("数据权限处理后SQL: {}", modifiedSql);
        }

        args[0] = getNewMappedStatement(mappedStatement, modifiedSql);

        return invocation.proceed();
    }

    private MappedStatement getNewMappedStatement(MappedStatement mappedStatement, String modifiedSql) {
        MappedStatement.Builder builder =
                new MappedStatement.Builder(mappedStatement.getConfiguration(),
                        mappedStatement.getId(),
                        new WrapperSqlSource(mappedStatement.getConfiguration(), mappedStatement.getSqlSource(), modifiedSql),
                        mappedStatement.getSqlCommandType());
        builder.cache(mappedStatement.getCache())
                .databaseId(mappedStatement.getDatabaseId())
                .fetchSize(mappedStatement.getFetchSize())
                .flushCacheRequired(mappedStatement.isFlushCacheRequired())
                .keyColumn(Objects.isNull(mappedStatement.getKeyColumns()) ? null : String.join(",", mappedStatement.getKeyColumns()))
                .keyGenerator(mappedStatement.getKeyGenerator())
                .keyProperty(Objects.isNull(mappedStatement.getKeyProperties()) ? null : String.join(",", mappedStatement.getKeyProperties()))
                .lang(mappedStatement.getLang())
                .parameterMap(mappedStatement.getParameterMap())
                .resource(mappedStatement.getResource())
                .resultMaps(mappedStatement.getResultMaps())
                .resultOrdered(mappedStatement.isResultOrdered())
                .resultSets(Objects.isNull(mappedStatement.getResultSets()) ? null : String.join(",", mappedStatement.getResultSets()))
                .resultSetType(mappedStatement.getResultSetType())
                .statementType(mappedStatement.getStatementType())
                .timeout(mappedStatement.getTimeout())
                .useCache(mappedStatement.isUseCache());
        return builder.build();
    }

    /**
     * 获取方法上的数据权限注解
     */
    private DataPermission getDataScopeAnnotation(MappedStatement ms) {
        String id = ms.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        String methodName = id.substring(id.lastIndexOf(".") + 1);

        try {
            Class<?> clazz = Class.forName(className);
            // 先查找方法上的注解
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    DataPermission dataPermission = AnnotationUtils.findAnnotation(method, DataPermission.class);
                    if (dataPermission != null) {
                        return dataPermission;
                    }
                }
            }
            // 再查找类上的注解
            return AnnotationUtils.findAnnotation(clazz, DataPermission.class);
        } catch (ClassNotFoundException e) {
            log.error("获取数据权限注解失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 处理SQL，添加数据权限条件 - 使用JSqlParser解析和处理SQL
     *
     * @param sql            原始SQL
     * @param dataPermission 数据权限注解
     * @return 处理后的SQL
     */
    private String processSql(String sql, DataPermission dataPermission) {
        try {
            // 如果是ALL类型，不做处理
            if (DataPermissionType.ALL.equals(dataPermission.type())) {
                return sql;
            }

            // 解析SQL
            Statement statement = CCJSqlParserUtil.parse(sql);

            Select selectStatement = (Select) statement;
            if (!(selectStatement instanceof PlainSelect)) {
                // 暂不处理非简单查询（如UNION、子查询等）
                // 未来可以扩展支持更复杂的查询
                log.warn("暂不支持复杂SQL的数据权限控制，原SQL将不被修改: {}", sql);
                return sql;
            }

            PlainSelect plainSelect = selectStatement.getPlainSelect();

            // 构建数据权限条件
            Expression permissionCondition = buildPermissionCondition(dataPermission);
            if (permissionCondition == null) {
                return sql;
            }

            // 添加数据权限条件到SQL
            addConditionToSelect(plainSelect, permissionCondition);

            return statement.toString();
        } catch (JSQLParserException e) {
            log.error("解析SQL失败，将使用原始SQL: {}, 错误: {}", sql, e.getMessage());
        } catch (Exception e) {
            log.error("处理数据权限SQL异常: {}, 将使用原始SQL", e.getMessage());
        }
        return sql;
    }

    /**
     * 构建数据权限条件
     *
     * @param dataPermission 数据权限注解
     * @return 构建的权限条件表达式，如果无法构建返回null
     */
    private Expression buildPermissionCondition(DataPermission dataPermission) {

        if (DataPermissionType.CUSTOM.equals(dataPermission.type()) && StringUtils.hasText(dataPermission.customSql())) {
            try {
                return CCJSqlParserUtil.parseCondExpression(dataPermission.customSql());
            } catch (JSQLParserException e) {
                log.error("解析自定义数据权限条件失败: {}, 错误: {}", dataPermission.customSql(), e.getMessage());
                return null;
            }
        }

        Expression permissionCondition = null;
        if (dataPermission.value() != null) {
            String scopeType = dataPermission.type();
            for (DataColumn dataColumn : dataPermission.value()) {
                Optional<String> sqlConditionOptional = handlerFactory.getSqlCondition(scopeType, dataColumn);
                if (sqlConditionOptional.isPresent()) {
                    String condition = sqlConditionOptional.get();
                    try {
                        Expression conditionExpression = CCJSqlParserUtil.parseCondExpression(condition);
                        if (permissionCondition == null) {
                            permissionCondition = conditionExpression;
                        } else {
                            permissionCondition = new AndExpression(permissionCondition, conditionExpression);
                        }
                    } catch (JSQLParserException e) {
                        log.error("解析数据权限条件失败: {}, 错误: {}", condition, e.getMessage());
                    }
                }
            }
        }

        return permissionCondition;
    }

    /**
     * 将条件添加到查询中
     *
     * @param plainSelect PlainSelect对象
     * @param condition   要添加的条件
     */
    private void addConditionToSelect(PlainSelect plainSelect, Expression condition) {
        if (plainSelect.getWhere() == null) {
            // 如果没有WHERE子句，直接设置
            plainSelect.setWhere(condition);
            return;
        }
        // 如果有WHERE子句，添加AND条件
        plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), condition));
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以在这里读取配置信息
    }

    public class WrapperSqlSource implements SqlSource {

        private final Configuration configuration;

        private final SqlSource sqlSource;

        private final String newSql;

        public WrapperSqlSource(Configuration configuration, SqlSource sqlSource, String newSql) {
            this.configuration = configuration;
            this.sqlSource = sqlSource;
            this.newSql = newSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            // 获取原始 BoundSql
            BoundSql boundSql = sqlSource.getBoundSql(parameterObject);

            // 保留原始的参数映射和属性
            BoundSql newBoundSql = new BoundSql(
                    configuration,
                    newSql,
                    boundSql.getParameterMappings(),
                    boundSql.getParameterObject()
            );

            // 复制原始 BoundSql 的附加参数
            boundSql.getAdditionalParameters().forEach(newBoundSql::setAdditionalParameter);

            return newBoundSql;
        }
    }
}