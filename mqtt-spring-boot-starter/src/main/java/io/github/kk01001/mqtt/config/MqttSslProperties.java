package io.github.kk01001.mqtt.config;

import lombok.Data;

/**
 * MQTT SSL/TLS 配置
 *
 * @author kk01001
 */
@Data
public class MqttSslProperties {

    /**
     * 是否启用 SSL/TLS
     */
    private Boolean enabled = false;

    /**
     * 密钥库路径
     */
    private String keyStore;

    /**
     * 密钥库密码
     */
    private String keyStorePassword;

    /**
     * 密钥库类型（JKS、PKCS12）
     */
    private String keyStoreType = "JKS";

    /**
     * 信任库路径
     */
    private String trustStore;

    /**
     * 信任库密码
     */
    private String trustStorePassword;

    /**
     * 信任库类型（JKS、PKCS12）
     */
    private String trustStoreType = "JKS";

    /**
     * 协议版本（TLSv1.2、TLSv1.3）
     */
    private String protocol = "TLSv1.2";

    /**
     * 是否验证服务器证书
     */
    private Boolean verifyServerCertificate = true;
}
