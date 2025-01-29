package io.github.kk01001.crypto;

public interface ParamsCryptoProvider {

    /**
     * 加密
     */
    String encrypt(String content);

    /**
     * 解密
     */
    String decrypt(String content);

    /**
     * 刷新密钥
     */
    void refreshKey(String key);

    /**
     * 刷新RSA密钥对
     */
    void refreshRSAKey(String publicKey, String privateKey);
} 