package io.github.archer099.signature.store;

import io.github.archer099.signature.properties.SignatureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description 基于配置的 AppSecret 存储实现
 */
@Slf4j
@RequiredArgsConstructor
public class ConfigAppSecretStore implements AppSecretStore {
    
    private final SignatureProperties properties;
    
    @Override
    public String getSecret(String appKey) {
        return properties.getApps().get(appKey);
    }
    
    @Override
    public boolean isValid(String appKey) {
        return properties.getApps().containsKey(appKey);
    }
}
