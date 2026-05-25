package io.github.archer099.signature.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description 签名验证结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureResult {
    
    /**
     * 是否验证通过
     */
    private boolean valid;
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * AppKey
     */
    private String appKey;
    
    /**
     * 请求时间戳
     */
    private Long timestamp;
    
    /**
     * 请求随机数
     */
    private String nonce;
    
    /**
     * 请求签名
     */
    private String signature;
    
    /**
     * 服务端计算的签名
     */
    private String expectedSignature;
    
    public static SignatureResult success() {
        return SignatureResult.builder().valid(true).build();
    }
    
    public static SignatureResult success(String appKey) {
        return SignatureResult.builder().valid(true).appKey(appKey).build();
    }
    
    public static SignatureResult fail(String errorCode, String errorMessage) {
        return SignatureResult.builder()
                .valid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
