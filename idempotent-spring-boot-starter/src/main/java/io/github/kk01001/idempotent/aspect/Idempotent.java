package io.github.kk01001.idempotent.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author linshiqiang
 * @date 2024-12-03 11:28:00
 * @description
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * key前缀
     */
    String keyPrefix() default "idempotent:";

    /**
     * 幂等key el
     * 为空默认 请求体md5
     */
    String key() default "";

    /**
     * 过期时间s
     */
    long expire() default 10;

}
