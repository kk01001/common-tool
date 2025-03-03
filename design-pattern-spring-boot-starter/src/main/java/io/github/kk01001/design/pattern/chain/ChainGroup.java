package io.github.kk01001.design.pattern.chain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 用于标识处理器所属的责任链分组
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ChainGroup {
    
    /**
     * 分组名称
     * @return 分组名称
     */
    String value();
    
    /**
     * 处理器顺序，值越小优先级越高
     * @return 顺序值
     */
    int order() default 0;
}