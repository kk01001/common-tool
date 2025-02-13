package io.github.kk01001.crypto.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamsCrypto {

    /**
     * 请求参数是否解密
     */
    boolean requestDecrypt() default true;

    /**
     * 响应参数是否加密
     */
    boolean responseEncrypt() default true;

} 