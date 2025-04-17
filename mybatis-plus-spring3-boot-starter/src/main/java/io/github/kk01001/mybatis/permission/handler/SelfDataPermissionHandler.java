package io.github.kk01001.mybatis.permission.handler;

import io.github.kk01001.mybatis.permission.annotations.DataColumn;
import io.github.kk01001.mybatis.permission.service.UserPermissionService;
import lombok.RequiredArgsConstructor;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 用户数据权限处理器
 */
@RequiredArgsConstructor
public class SelfDataPermissionHandler<T> extends AbstractDataPermissionHandler<T> implements DataPermissionHandler {

    private final UserPermissionService<T> userPermissionService;

    @Override
    public String getType() {
        return DataPermissionType.SELF;
    }

    @Override
    public String getSqlCondition(DataColumn dataColumn) {
        String columnName = getColumnName(dataColumn);
        T userId = getCurrentSelfId(dataColumn);
        if (userId == null) {
            return null;
        }
        return columnName + " = " + userId;
    }

    /**
     * 获取完整的列名
     */
    private String getColumnName(DataColumn dataColumn) {
        if (dataColumn.alias() == null || dataColumn.alias().isEmpty()) {
            return dataColumn.name();
        }
        return dataColumn.alias() + "." + dataColumn.name();
    }

    @Override
    protected UserPermissionService<T> getUserPermissionService() {
        return userPermissionService;
    }
}
