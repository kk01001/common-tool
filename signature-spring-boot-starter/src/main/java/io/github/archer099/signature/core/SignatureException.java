package io.github.archer099.signature.core;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description 签名验证异常
 */
public class SignatureException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String errorCode;
    
    /**
     * 签名验证结果
     */
    private final SignatureResult result;
    
    public SignatureException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.result = SignatureResult.fail(errorCode, message);
    }
    
    public SignatureException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.result = SignatureResult.fail(errorCode, message);
    }
    
    public SignatureException(SignatureResult result) {
        super(result.getErrorMessage());
        this.errorCode = result.getErrorCode();
        this.result = result;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public SignatureResult getResult() {
        return result;
    }
    
    /**
     * 签名缺失
     */
    public static SignatureException signatureMissing() {
        return new SignatureException("SIGNATURE_MISSING", "签名参数缺失");
    }
    
    /**
     * 签名无效
     */
    public static SignatureException signatureInvalid() {
        return new SignatureException("SIGNATURE_INVALID", "签名验证失败");
    }
    
    /**
     * 时间戳过期
     */
    public static SignatureException timestampExpired() {
        return new SignatureException("TIMESTAMP_EXPIRED", "请求已过期");
    }
    
    /**
     * Nonce 重复
     */
    public static SignatureException nonceReplay() {
        return new SignatureException("NONCE_REPLAY", "请求重复提交");
    }
    
    /**
     * AppKey 无效
     */
    public static SignatureException appKeyInvalid() {
        return new SignatureException("APPKEY_INVALID", "AppKey 无效");
    }
}
