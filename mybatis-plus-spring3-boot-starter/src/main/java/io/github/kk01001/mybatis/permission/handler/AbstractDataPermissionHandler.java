package io.github.kk01001.mybatis.permission.handler;

import io.github.kk01001.mybatis.permission.annotations.DataColumn;
import io.github.kk01001.mybatis.permission.service.UserPermissionService;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2025-04-16 14:41
 * @description
 */
public abstract class AbstractDataPermissionHandler<T> {

    public T getCurrentSelfId(DataColumn dataColumn) {
        return getUserPermissionService().getCurrentSelfId(dataColumn);
    }

    public List<T> getSelfAndSubIds(DataColumn dataColumn) {
        return getUserPermissionService().getSelfAndSubIds(dataColumn);
    }

    protected abstract UserPermissionService<T> getUserPermissionService();

}
