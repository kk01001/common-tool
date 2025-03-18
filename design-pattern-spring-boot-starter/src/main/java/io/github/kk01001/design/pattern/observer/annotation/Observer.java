package io.github.kk01001.design.pattern.observer.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 用于标识观察者类，指定其所属的主题分组和优先级
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Observer {
    /**
     * 主题分组名称，用于将观察者与特定主题关联
     * @return 主题分组名称
     */
    String topic() default "";
    
    /**
     * 观察者优先级，数值越小优先级越高
     * @return 优先级值
     */
    int order() default 0;
}