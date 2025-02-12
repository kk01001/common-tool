package io.github.kk01001.crypto.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CryptoField {
    /**
     * 是否加密
     */
    boolean encrypt() default true;

    /**
     * 是否解密
     */
    boolean decrypt() default true;
} 