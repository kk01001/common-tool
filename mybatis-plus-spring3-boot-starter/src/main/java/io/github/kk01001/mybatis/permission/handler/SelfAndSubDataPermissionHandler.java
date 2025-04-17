package io.github.kk01001.mybatis.permission.handler;

import io.github.kk01001.mybatis.permission.annotations.DataColumn;
import io.github.kk01001.mybatis.permission.service.UserPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 本身及下级的数据权限处理器
 */
@RequiredArgsConstructor
public class SelfAndSubDataPermissionHandler<T> extends AbstractDataPermissionHandler<T> implements DataPermissionHandler {

    private final UserPermissionService<T> userPermissionService;

    @Override
    public String getType() {
        return DataPermissionType.SELF_AND_SUB;
    }

    @Override
    public String getSqlCondition(DataColumn dataColumn) {
        List<T> deptIds = getSelfAndSubIds(dataColumn);
        if (CollectionUtils.isEmpty(deptIds)) {
            return null;
        }

        String columnName = getColumnName(dataColumn);
        if (!StringUtils.hasText(columnName)) {
            return null;
        }

        String deptIdsStr = deptIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        return columnName + " IN (" + deptIdsStr + ")";
    }

    /**
     * 获取完整列名
     */
    protected String getColumnName(DataColumn dataColumn) {
        if (dataColumn == null) {
            return null;
        }

        String columnName = dataColumn.name();
        if (!StringUtils.hasText(columnName)) {
            return null;
        }

        String alias = dataColumn.alias();
        if (StringUtils.hasText(alias)) {
            return alias + "." + columnName;
        }

        return columnName;
    }

    @Override
    protected UserPermissionService<T> getUserPermissionService() {
        return userPermissionService;
    }
}
