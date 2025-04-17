package io.github.kk01001.mybatis.permission.service;

import io.github.kk01001.mybatis.permission.annotations.DataColumn;

import java.util.List;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 用户权限服务接口，用于获取用户权限数据
 */
public interface UserPermissionService<T> {

    /**
     * 获取当前 本身ID
     *
     * @return 当前用户ID
     */
    T getCurrentSelfId(DataColumn dataColumn);

    /**
     * 获取 本身及下级ID集合
     *
     * @return ID集合
     */
    List<T> getSelfAndSubIds(DataColumn dataColumn);

    /**
     * 判断当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    default boolean isAdmin() {
        return false;
    }

    /**
     * 获取用户自定义数据范围
     *
     * @return 自定义数据范围
     */
    List<Long> getCustomDataScope(DataColumn dataColumn);
}
