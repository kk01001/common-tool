package io.github.kk01001.mybatis.permission.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 数据列注解，用于配置数据权限的列信息
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataColumn {
    
    /**
     * 表别名
     */
    String alias() default "";
    
    /**
     * 列名
     */
    String name() default "";
    
    /**
     * 列类型
     * 可选值：deptId, userId, customField
     * 默认为空，会根据列名自动判断
     */
    String type() default "";
}
