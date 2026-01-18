package io.github.kk01001.signature.handler;

import cn.hutool.core.util.StrUtil;
import io.github.kk01001.signature.core.SignatureException;
import io.github.kk01001.signature.core.SignatureResult;
import io.github.kk01001.signature.properties.SignatureProperties;
import io.github.kk01001.signature.store.AppSecretStore;
import io.github.kk01001.signature.store.NonceStore;
import io.github.kk01001.signature.util.SignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2026-01-18 13:23:23
 * @description 签名处理器
 */
@Slf4j
@RequiredArgsConstructor
public class SignatureHandler {
    
    private final SignatureProperties properties;
    private final AppSecretStore appSecretStore;
    private final NonceStore nonceStore;
    
    /**
     * 验证签名
     *
     * @param appKey    应用标识
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param params    请求参数
     * @return 验证结果
     */
    public SignatureResult verify(String appKey, String signature, String timestamp, 
                                   String nonce, Map<String, String> params) {
        return verify(appKey, signature, timestamp, nonce, params, null);
    }
    
    /**
     * 验证签名（带请求体）
     *
     * @param appKey    应用标识
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param params    请求参数
     * @param body      请求体
     * @return 验证结果
     */
    public SignatureResult verify(String appKey, String signature, String timestamp, 
                                   String nonce, Map<String, String> params, String body) {
        return verify(appKey, signature, timestamp, nonce, params, body, properties.isParseJsonBody());
    }
    
    /**
     * 验证签名（带请求体，指定解析模式）
     *
     * @param appKey        应用标识
     * @param signature     签名
     * @param timestamp     时间戳
     * @param nonce         随机数
     * @param params        请求参数
     * @param body          请求体
     * @param parseJsonBody 是否解析 JSON Body 字段
     * @return 验证结果
     */
    public SignatureResult verify(String appKey, String signature, String timestamp, 
                                   String nonce, Map<String, String> params, String body, 
                                   boolean parseJsonBody) {
        
        if (properties.isDebug()) {
            log.debug("签名验证开始 - appKey: {}, signature: {}, timestamp: {}, nonce: {}", 
                    appKey, signature, timestamp, nonce);
        }
        
        // 1. 验证必填参数
        if (StrUtil.isBlank(appKey)) {
            return SignatureResult.fail("APPKEY_MISSING", "AppKey 参数缺失");
        }
        if (StrUtil.isBlank(signature)) {
            return SignatureResult.fail("SIGNATURE_MISSING", "签名参数缺失");
        }
        
        // 2. 验证时间戳
        if (properties.isTimestampEnabled()) {
            if (StrUtil.isBlank(timestamp)) {
                return SignatureResult.fail("TIMESTAMP_MISSING", "时间戳参数缺失");
            }
            
            try {
                long ts = Long.parseLong(timestamp);
                if (!SignatureUtil.isTimestampValid(ts, properties.getTimestampTolerance().toSeconds())) {
                    return SignatureResult.fail("TIMESTAMP_EXPIRED", "请求已过期");
                }
            } catch (NumberFormatException e) {
                return SignatureResult.fail("TIMESTAMP_INVALID", "时间戳格式无效");
            }
        }
        
        // 3. 验证 Nonce（防重放）
        if (properties.isNonceEnabled()) {
            if (StrUtil.isBlank(nonce)) {
                return SignatureResult.fail("NONCE_MISSING", "Nonce 参数缺失");
            }
            
            // 检查 nonce 是否已使用
            if (!nonceStore.storeIfAbsent(nonce, properties.getNonceTtl())) {
                return SignatureResult.fail("NONCE_REPLAY", "请求重复提交");
            }
        }
        
        // 4. 获取 AppSecret
        String secret = appSecretStore.getSecret(appKey);
        if (StrUtil.isBlank(secret)) {
            return SignatureResult.fail("APPKEY_INVALID", "AppKey 无效");
        }
        
        // 5. 构建签名参数（包含 appKey、timestamp、nonce）
        Map<String, String> signParams = new HashMap<>(params);
        signParams.put("appKey", appKey);
        if (StrUtil.isNotBlank(timestamp)) {
            signParams.put("timestamp", timestamp);
        }
        if (StrUtil.isNotBlank(nonce)) {
            signParams.put("nonce", nonce);
        }
        
        // 6. 计算签名
        String expectedSign;
        String signContent;
        
        if (StrUtil.isNotBlank(body)) {
            if (parseJsonBody) {
                // JSON Body 字段解析后参与排序签名
                expectedSign = SignatureUtil.signWithJsonBody(signParams, body, secret, 
                        properties.getAlgorithm(), properties.getJsonBodyPrefix());
                
                // 调试信息：构建完整签名内容
                Map<String, String> allParams = new java.util.TreeMap<>(signParams);
                try {
                    cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(body);
                    flattenJsonForDebug(json, properties.getJsonBodyPrefix(), allParams);
                } catch (Exception e) {
                    allParams.put("body", body);
                }
                signContent = SignatureUtil.buildSignContent(allParams) + "&secret=" + secret;
            } else {
                // 原始方式：Body 作为整体字符串
                signContent = SignatureUtil.buildSignContent(signParams) + "&body=" + body + "&secret=" + secret;
                expectedSign = SignatureUtil.sign(signParams, body, secret, properties.getAlgorithm());
            }
        } else {
            signContent = SignatureUtil.buildSignContent(signParams) + "&secret=" + secret;
            expectedSign = SignatureUtil.sign(signParams, secret, properties.getAlgorithm());
        }
        
        if (properties.isDebug()) {
            log.debug("========== 签名验证调试信息 ==========");
            log.debug("parseJsonBody模式: {}", parseJsonBody);
            log.debug("原始参数: {}", signParams);
            log.debug("请求体(body): {}", body);
            log.debug("加密前完整字符串: {}", signContent);
            log.debug("服务端计算签名: {}", expectedSign);
            log.debug("客户端传递签名: {}", signature);
            log.debug("签名是否匹配: {}", StrUtil.equalsIgnoreCase(expectedSign, signature));
            log.debug("======================================");
        }
        
        // 6. 验证签名
        if (!StrUtil.equalsIgnoreCase(expectedSign, signature)) {
            SignatureResult result = SignatureResult.fail("SIGNATURE_INVALID", "签名验证失败");
            result.setSignature(signature);
            result.setExpectedSignature(expectedSign);
            return result;
        }
        
        // 验证通过
        SignatureResult result = SignatureResult.success(appKey);
        result.setTimestamp(StrUtil.isNotBlank(timestamp) ? Long.parseLong(timestamp) : null);
        result.setNonce(nonce);
        result.setSignature(signature);
        return result;
    }
    
    /**
     * 生成签名
     *
     * @param appKey 应用标识
     * @param params 请求参数
     * @return 签名字符串
     */
    public String sign(String appKey, Map<String, String> params) {
        String secret = appSecretStore.getSecret(appKey);
        if (StrUtil.isBlank(secret)) {
            throw SignatureException.appKeyInvalid();
        }
        return SignatureUtil.sign(params, secret, properties.getAlgorithm());
    }
    
    /**
     * 生成签名（带请求体）
     *
     * @param appKey 应用标识
     * @param params 请求参数
     * @param body   请求体
     * @return 签名字符串
     */
    public String sign(String appKey, Map<String, String> params, String body) {
        String secret = appSecretStore.getSecret(appKey);
        if (StrUtil.isBlank(secret)) {
            throw SignatureException.appKeyInvalid();
        }
        
        if (properties.isParseJsonBody()) {
            return SignatureUtil.signWithJsonBody(params, body, secret, 
                    properties.getAlgorithm(), properties.getJsonBodyPrefix());
        }
        return SignatureUtil.sign(params, body, secret, properties.getAlgorithm());
    }
    
    /**
     * 将 JSON 对象扁平化（调试用）
     */
    private void flattenJsonForDebug(cn.hutool.json.JSONObject json, String prefix, Map<String, String> result) {
        for (String key : json.keySet()) {
            Object value = json.get(key);
            String fullKey = StrUtil.isBlank(prefix) ? key : prefix + key;
            
            if (value instanceof cn.hutool.json.JSONObject nestedJson) {
                flattenJsonForDebug(nestedJson, fullKey + ".", result);
            } else if (value instanceof cn.hutool.json.JSONArray jsonArray) {
                result.put(fullKey, jsonArray.toString());
            } else if (value != null) {
                result.put(fullKey, value.toString());
            }
        }
    }
}
