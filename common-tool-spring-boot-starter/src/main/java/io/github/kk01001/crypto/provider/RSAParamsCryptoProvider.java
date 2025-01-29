package io.github.kk01001.crypto.provider;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import io.github.kk01001.crypto.ParamsCryptoProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RSAParamsCryptoProvider implements ParamsCryptoProvider {

    private volatile RSA rsa;

    public RSAParamsCryptoProvider(String privateKey, String publicKey) {
        refreshRSAKey(publicKey, privateKey);
    }

    @Override
    public synchronized void refreshRSAKey(String publicKey, String privateKey) {
        this.rsa = new RSA(privateKey, publicKey);
    }

    @Override
    public void refreshKey(String key) {
        throw new UnsupportedOperationException("RSA不支持单密钥刷新");
    }

    @Override
    public String encrypt(String content) {
        try {
            return rsa.encryptHex(content, KeyType.PublicKey);
        } catch (Exception e) {
            log.error("RSA加密失败,content:{}", content, e);
            return content;
        }
    }

    @Override
    public String decrypt(String content) {
        try {
            return rsa.decryptStr(content, KeyType.PrivateKey);
        } catch (Exception e) {
            log.error("RSA解密失败,content:{}", content, e);
            return content;
        }
    }
} 