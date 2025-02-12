package io.github.kk01001.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.github.kk01001.mybatis.core.DynamicTableProperties;
import io.github.kk01001.mybatis.core.EasySqlInjector;
import io.github.kk01001.mybatis.core.RequestDataHelper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author kk01001
 * date 2022-09-20 21:51:00
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({DynamicTableProperties.class})
public class MybatisPlusConfiguration {

    /**
     * 批量新增sql
     */
    @Bean
    public EasySqlInjector easySqlInjector() {

        return new EasySqlInjector();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(DynamicTableProperties dynamicTableProperties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 添加非法SQL拦截器
        // interceptor.addInnerInterceptor(new IllegalSQLInnerInterceptor());

        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 如果配置多个插件, 切记分页最后添加

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();

        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            Map<String, String> tableRuleMap = dynamicTableProperties.getTableRule();
            if (CollectionUtils.isEmpty(tableRuleMap)) {
                return tableName;
            }
            String tableTemplate = tableRuleMap.get(tableName);
            if (!StringUtils.hasLength(tableTemplate)) {
                return tableName;
            }
            Map<String, Object> requestData = RequestDataHelper.getRequestData();
            return format(tableTemplate, requestData, true);
        });
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }

    /**
     * 格式化文本，使用 {varName} 占位<br>
     * map = {a: "aValue", b: "bValue"} format("{a} and {b}", map) ---=》 aValue and bValue
     *
     * @param template   文本模板，被替换的部分用 {key} 表示
     * @param map        参数值对
     * @param ignoreNull 是否忽略 {@code null} 值，忽略则 {@code null} 值对应的变量不被替换，否则替换为""
     * @return 格式化后的文本
     * @since 5.7.10
     */
    private String format(CharSequence template, Map<String, Object> map, boolean ignoreNull) {
        if (null == template) {
            return null;
        }
        if (null == map || map.isEmpty()) {
            return template.toString();
        }

        String template2 = template.toString();
        String value;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            value = String.valueOf(entry.getValue());
            if (!StringUtils.hasLength(value) && ignoreNull) {
                continue;
            }
            template2 = template2.replace("{" + entry.getKey() + "}", value);
        }
        return template2;
    }
}
