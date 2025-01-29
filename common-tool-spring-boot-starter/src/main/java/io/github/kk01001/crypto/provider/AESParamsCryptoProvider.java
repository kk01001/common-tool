package io.github.kk01001.crypto.provider;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
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
        this.aes = SecureUtil.aes(key.getBytes());
    }

    @Override
    public void refreshRSAKey(String publicKey, String privateKey) {
        throw new UnsupportedOperationException("AES不支持RSA密钥对");
    }

    @Override
    public String encrypt(String content) {
        try {
            return aes.encryptHex(content);
        } catch (Exception e) {
            log.error("AES加密失败,content:{}", content, e);
            return content;
        }
    }

    @Override
    public String decrypt(String content) {
        try {
            return aes.decryptStr(content);
        } catch (Exception e) {
            log.error("AES解密失败,content:{}", content, e);
            return content;
        }
    }
} 