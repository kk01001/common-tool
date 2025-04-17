package io.github.kk01001.mybatis.permission.handler;

import lombok.experimental.UtilityClass;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 数据权限类型常量定义
 */
@UtilityClass
public class DataPermissionType {

    /**
     * 全部数据权限
     */
    public static final String ALL = "ALL";

    /**
     * 本身及下级的数据权限
     */
    public static final String SELF_AND_SUB = "SELF_AND_SUB";

    /**
     * 仅本人数据权限
     */
    public static final String SELF = "SELF";

    /**
     * 自定义数据权限
     */
    public static final String CUSTOM = "CUSTOM";
}
