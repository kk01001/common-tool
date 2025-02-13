package io.github.kk01001.crypto.provider;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import io.github.kk01001.crypto.ParamsCryptoProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SM4ParamsCryptoProvider implements ParamsCryptoProvider {

    private volatile SM4 sm4;

    public SM4ParamsCryptoProvider(String key) {
        refreshKey(key);
    }

    @Override
    public synchronized void refreshKey(String key) {
        this.sm4 = SmUtil.sm4(key.getBytes());
    }

    @Override
    public void refreshRSAKey(String publicKey, String privateKey) {
        throw new UnsupportedOperationException("SM4不支持RSA密钥对");
    }

    @Override
    public String encrypt(String content) {
        try {
            return sm4.encryptHex(content);
        } catch (Exception e) {
            log.error("SM4加密失败,content:{}", content, e);
            return content;
        }
    }

    @Override
    public String decrypt(String content) {
        try {
            return sm4.decryptStr(content);
        } catch (Exception e) {
            log.error("SM4解密失败,content:{}", content, e);
            return content;
        }
    }

    @Override
    public String generateKey(int length) {
        if (length != 16) {
            throw new IllegalArgumentException("SM4密钥长度必须为16位");
        }

        byte[] key = SecureUtil.generateKey(SM4.ALGORITHM_NAME).getEncoded();
        return HexUtil.encodeHexStr(key);
    }
} 