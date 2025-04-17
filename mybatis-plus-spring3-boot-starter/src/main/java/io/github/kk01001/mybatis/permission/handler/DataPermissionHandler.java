package io.github.kk01001.mybatis.permission.handler;

import io.github.kk01001.mybatis.permission.annotations.DataColumn;

import java.util.List;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 数据权限处理器接口，用于生成SQL条件
 */
public interface DataPermissionHandler {

    /**
     * 获取数据权限类型
     *
     * @return 数据权限类型
     */
    String getType();

    /**
     * 获取SQL条件
     *
     * @param dataColumn 数据列配置
     * @return SQL条件语句
     */
    String getSqlCondition(DataColumn dataColumn);

    /**
     * 通过上下文获取用户ID
     *
     * @return 用户ID
     */
    default Long getUserId() {
        return -1L;
    }

    /**
     * 通过上下文获取部门ID
     *
     * @return 部门ID
     */
    default Long getDeptId() {
        return -1L;
    }

    /**
     * 通过上下文获取部门ID列表
     *
     * @return 部门ID列表
     */
    default List<Long> getDeptIds() {
        return null;
    }
}
