package io.github.archer099.signature.annotation;

import java.lang.annotation.*;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description 忽略签名验证注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreSignature {
}
