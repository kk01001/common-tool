package io.github.kk01001.mybatis.permission.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 数据权限注解，用于标记需要数据权限控制的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 数据权限类型 默认为空，表示使用上下文中的类型
     */
    String type() default "";

    /**
     * 自定义SQL条件 当type为CUSTOM时生效
     */
    String customSql() default "";

    /**
     * 是否忽略权限控制
     */
    boolean ignore() default false;

    /**
     * 数据列配置
     */
    DataColumn[] value() default {};
}
