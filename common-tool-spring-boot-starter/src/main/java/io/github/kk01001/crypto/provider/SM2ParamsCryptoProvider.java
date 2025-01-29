package io.github.kk01001.crypto.provider;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import io.github.kk01001.crypto.ParamsCryptoProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SM2ParamsCryptoProvider implements ParamsCryptoProvider {

    private volatile SM2 sm2;

    public SM2ParamsCryptoProvider(String privateKey, String publicKey) {
        refreshRSAKey(publicKey, privateKey);
    }

    @Override
    public synchronized void refreshRSAKey(String publicKey, String privateKey) {
        this.sm2 = SmUtil.sm2(privateKey, publicKey);
    }

    @Override
    public void refreshKey(String key) {
        throw new UnsupportedOperationException("SM2不支持单密钥刷新");
    }

    @Override
    public String encrypt(String content) {
        try {
            return sm2.encryptBase64(content, KeyType.PublicKey);
        } catch (Exception e) {
            log.error("SM2加密失败,content:{}", content, e);
            return content;
        }
    }

    @Override
    public String decrypt(String content) {
        try {
            return sm2.decryptStr(content, KeyType.PrivateKey);
        } catch (Exception e) {
            log.error("SM2解密失败,content:{}", content, e);
            return content;
        }
    }
} 