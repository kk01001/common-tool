package io.github.kk01001.mybatis.datasource;

import javax.sql.DataSource;
import java.lang.reflect.Field;

/**
 * @author kk01001
 * @date 2024-08-17 10:18:00
 * @description
 */
public class ReflectionDataSourceUtils {

    /**
     * 通过反射获取数据源的用户名
     *
     * @param dataSource 数据源对象
     * @return 用户名
     */
    public static String getUsername(DataSource dataSource) {
        return getFieldValue(dataSource, "username");
    }

    /**
     * 通过反射获取数据源的密码
     *
     * @param dataSource 数据源对象
     * @return 密码
     */
    public static String getPassword(DataSource dataSource) {
        return getFieldValue(dataSource, "password");
    }

    /**
     * 通过反射获取数据源的URL
     *
     * @param dataSource 数据源对象
     * @return URL
     */
    public static String getUrl(DataSource dataSource) {
        return getFieldValue(dataSource, "url");
    }

    /**
     * 通过反射获取数据源的type
     *
     * @param dataSource 数据源对象
     * @return URL
     */
    public static String getType(DataSource dataSource) {
        return getFieldValue(dataSource, "type");
    }

    /**
     * 通过反射获取指定字段的值
     *
     * @param dataSource 数据源对象
     * @param fieldName  字段名
     * @return 字段值
     */
    private static String getFieldValue(DataSource dataSource, String fieldName) {
        try {
            Field field = dataSource.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (String) field.get(dataSource);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value: " + fieldName, e);
        }
    }
}
