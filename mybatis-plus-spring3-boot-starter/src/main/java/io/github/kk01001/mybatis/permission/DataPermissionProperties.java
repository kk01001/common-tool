package io.github.kk01001.mybatis.permission;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 数据权限配置属性
 */
@Data
@ConfigurationProperties(prefix = "mybatis.data.permission")
public class DataPermissionProperties {

    /**
     * 是否启用数据权限拦截
     */
    private boolean enabled = true;

    /**
     * SQL拦截日志开关
     */
    private boolean sqlLog = false;
}
