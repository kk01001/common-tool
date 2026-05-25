package io.github.archer099.signature.core;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description 签名算法枚举
 */
public enum SignatureAlgorithm {
    
    /**
     * MD5 签名
     */
    MD5,
    
    /**
     * SHA1 签名
     */
    SHA1,
    
    /**
     * SHA256 签名
     */
    SHA256,
    
    /**
     * SHA512 签名
     */
    SHA512,
    
    /**
     * HMAC-SHA256 签名
     */
    HMAC_SHA256,
    
    /**
     * HMAC-SHA512 签名
     */
    HMAC_SHA512
}
