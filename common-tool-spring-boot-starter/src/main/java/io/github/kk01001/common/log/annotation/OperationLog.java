package io.github.kk01001.common.log.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作类型
     */
    OperationType type() default OperationType.OTHER;
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 是否保存请求参数
     */
    boolean saveRequestData() default true;
    
    /**
     * 是否保存响应数据
     */
    boolean saveResponseData() default true;
} 