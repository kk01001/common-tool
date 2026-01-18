package io.github.kk01001.signature.annotation;

import java.lang.annotation.*;

/**
 * @author kk01001
 * @date 2026-01-18 13:23:23
 * @description 签名验证注解，标记需要进行签名验证的方法或类
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SignatureVerify {
    
    /**
     * 是否启用签名验证
     */
    boolean enabled() default true;
    
    /**
     * 是否验证请求体
     */
    boolean verifyBody() default false;
    
    /**
     * 是否将 JSON Body 字段解析后参与签名排序
     * true: JSON 字段解析后与其他参数合并排序签名（字段级别校验）
     * false: JSON Body 作为整体字符串参与签名（原始方式）
     * 仅在 verifyBody=true 时生效
     */
    boolean parseJsonBody() default false;
    
    /**
     * 是否验证时间戳
     */
    boolean verifyTimestamp() default true;
    
    /**
     * 是否验证 Nonce（防重放）
     */
    boolean verifyNonce() default true;
}
