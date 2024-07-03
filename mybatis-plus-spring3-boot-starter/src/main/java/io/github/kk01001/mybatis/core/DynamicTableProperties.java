package io.github.kk01001.mybatis.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author kk01001
 * date 2023-07-22 15:23:00
 * 动态表名规则
 */
@ConfigurationProperties(prefix = "mybatis.dynamic")
public class DynamicTableProperties {

    private Map<String, String> tableRule;

    public Map<String, String> getTableRule() {
        return tableRule;
    }

    public void setTableRule(Map<String, String> tableRule) {
        this.tableRule = tableRule;
    }
}
