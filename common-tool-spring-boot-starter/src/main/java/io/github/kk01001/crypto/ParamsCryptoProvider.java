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

    /**
     * 生成对称加密密钥
     * 
     * @param length 密钥长度(AES:16/24/32, SM4:16)
     * @return 随机生成的密钥
     */
    default String generateKey(int length) {
        throw new UnsupportedOperationException("不支持生成对称加密密钥");
    }

    /**
     * 生成RSA密钥对
     * 
     * @param keySize 密钥大小(一般为1024/2048/4096)
     * @return RSA密钥对(包含公钥和私钥)
     */
    default RSAKeyPair generateRSAKeyPair(int keySize) {
        throw new UnsupportedOperationException("不支持生成RSA密钥对");
    }

    /**
     * RSA密钥对
     */
    class RSAKeyPair {
        private final String publicKey;
        private final String privateKey;

        public RSAKeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }
    }
} 