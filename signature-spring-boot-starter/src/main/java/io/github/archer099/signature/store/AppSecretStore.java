package io.github.archer099.signature.store;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description AppSecret 存储接口，用于获取应用密钥，由用户实现具体存储逻辑
 */
public interface AppSecretStore {
    
    /**
     * 根据 AppKey 获取 AppSecret
     *
     * @param appKey 应用标识
     * @return AppSecret，如果不存在返回 null
     */
    String getSecret(String appKey);
    
    /**
     * 检查 AppKey 是否有效
     *
     * @param appKey 应用标识
     * @return 是否有效
     */
    default boolean isValid(String appKey) {
        return getSecret(appKey) != null;
    }
    
    /**
     * 获取应用名称（可选）
     *
     * @param appKey 应用标识
     * @return 应用名称
     */
    default String getAppName(String appKey) {
        return appKey;
    }
}
