package io.github.kk01001.crypto.provider;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import io.github.kk01001.crypto.ParamsCryptoProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AESParamsCryptoProvider implements ParamsCryptoProvider {

    private volatile AES aes;

    public AESParamsCryptoProvider(String key) {
        refreshKey(key);
    }

    @Override
    public synchronized void refreshKey(String key) {
        this.aes = SecureUtil.aes(HexUtil.decodeHex(key));
    }

    @Override
    public void refreshRSAKey(String publicKey, String privateKey) {
        throw new UnsupportedOperationException("AES不支持RSA密钥对");
    }

    @Override
    public String encrypt(String content) {
        return aes.encryptHex(content);
    }

    @Override
    public String decrypt(String content) {
        return aes.decryptStr(content);
    }

    @Override
    public String generateKey(int length) {
        // AES 算法要求密钥的长度必须是 128 位（16 字节）、192 位（24 字节） 或 256 位（32 字节）
        if (length != 16 && length != 24 && length != 32) {
            throw new IllegalArgumentException("AES密钥长度必须为16/24/32位");
        }
        byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue(), length).getEncoded();
        return HexUtil.encodeHexStr(key);
    }
}