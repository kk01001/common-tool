package io.github.kk01001.signature.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.kk01001.signature.core.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author kk01001
 * @date 2026-01-18 13:23:23
 * @description 签名工具类
 */
@Slf4j
public final class SignatureUtil {
    
    private SignatureUtil() {
    }
    
    /**
     * 生成签名
     *
     * @param params    请求参数（已排序）
     * @param secret    密钥
     * @param algorithm 签名算法
     * @return 签名字符串
     */
    public static String sign(Map<String, String> params, String secret, SignatureAlgorithm algorithm) {
        // 1. 参数排序并拼接
        String content = buildSignContent(params);
        
        // 2. 拼接密钥
        String signStr = content + "&secret=" + secret;
        
        // 3. 计算签名
        return calculateSign(signStr, secret, algorithm);
    }
    
    /**
     * 生成签名（带 body，原始字符串方式）
     *
     * @param params    请求参数
     * @param body      请求体
     * @param secret    密钥
     * @param algorithm 签名算法
     * @return 签名字符串
     */
    public static String sign(Map<String, String> params, String body, String secret, SignatureAlgorithm algorithm) {
        // 1. 参数排序并拼接
        String content = buildSignContent(params);
        
        // 2. 拼接 body
        if (StrUtil.isNotBlank(body)) {
            content = content + "&body=" + body;
        }
        
        // 3. 拼接密钥
        String signStr = content + "&secret=" + secret;
        
        // 4. 计算签名
        return calculateSign(signStr, secret, algorithm);
    }
    
    /**
     * 生成签名（JSON body 字段参与排序）
     * 将 JSON body 解析后，字段与其他参数合并排序
     *
     * @param params       请求参数
     * @param jsonBody     JSON 请求体
     * @param secret       密钥
     * @param algorithm    签名算法
     * @param bodyPrefix   body 字段前缀（如 "body."）
     * @return 签名字符串
     */
    public static String signWithJsonBody(Map<String, String> params, String jsonBody, 
                                           String secret, SignatureAlgorithm algorithm,
                                           String bodyPrefix) {
        // 1. 合并参数
        Map<String, String> allParams = new TreeMap<>(params);
        
        // 2. 解析 JSON body 并合并
        if (StrUtil.isNotBlank(jsonBody)) {
            try {
                JSONObject json = JSONUtil.parseObj(jsonBody);
                flattenJson(json, bodyPrefix, allParams);
            } catch (Exception e) {
                log.warn("解析 JSON body 失败，使用原始字符串: {}", e.getMessage());
                allParams.put("body", jsonBody);
            }
        }
        
        // 3. 排序并拼接
        String content = buildSignContent(allParams);
        
        // 4. 拼接密钥
        String signStr = content + "&secret=" + secret;
        
        // 5. 计算签名
        return calculateSign(signStr, secret, algorithm);
    }
    
    /**
     * 生成签名（JSON body 字段参与排序，默认前缀 "body."）
     */
    public static String signWithJsonBody(Map<String, String> params, String jsonBody, 
                                           String secret, SignatureAlgorithm algorithm) {
        return signWithJsonBody(params, jsonBody, secret, algorithm, "body.");
    }
    
    /**
     * 将 JSON 对象扁平化为 key-value
     *
     * @param json   JSON 对象
     * @param prefix 前缀
     * @param result 结果 Map
     */
    private static void flattenJson(JSONObject json, String prefix, Map<String, String> result) {
        for (String key : json.keySet()) {
            Object value = json.get(key);
            String fullKey = StrUtil.isBlank(prefix) ? key : prefix + key;
            
            if (value instanceof JSONObject nestedJson) {
                // 递归处理嵌套对象
                flattenJson(nestedJson, fullKey + ".", result);
            } else if (value instanceof cn.hutool.json.JSONArray jsonArray) {
                // 数组转为 JSON 字符串
                result.put(fullKey, jsonArray.toString());
            } else if (value != null) {
                result.put(fullKey, value.toString());
            }
        }
    }
    
    /**
     * 构建签名内容
     *
     * @param params 请求参数
     * @return 签名内容字符串
     */
    public static String buildSignContent(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        // 过滤空值并排序
        TreeMap<String, String> sortedParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (StrUtil.isNotBlank(entry.getValue())) {
                sortedParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        // 拼接参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        return sb.toString();
    }
    
    /**
     * 计算签名
     *
     * @param content   签名内容
     * @param secret    密钥
     * @param algorithm 签名算法
     * @return 签名字符串
     */
    public static String calculateSign(String content, String secret, SignatureAlgorithm algorithm) {
        return switch (algorithm) {
            case MD5 -> SecureUtil.md5(content);
            case SHA1 -> SecureUtil.sha1(content);
            case SHA256 -> SecureUtil.sha256(content);
            case SHA512 -> {
                // Hutool 没有直接的 sha512 方法，使用 DigestUtil
                yield cn.hutool.crypto.digest.DigestUtil.sha512Hex(content);
            }
            case HMAC_SHA256 -> {
                HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, secret.getBytes(StandardCharsets.UTF_8));
                yield hmac.digestHex(content);
            }
            case HMAC_SHA512 -> {
                HMac hmac = new HMac(HmacAlgorithm.HmacSHA512, secret.getBytes(StandardCharsets.UTF_8));
                yield hmac.digestHex(content);
            }
        };
    }
    
    /**
     * 验证签名
     *
     * @param params         请求参数
     * @param signature      请求签名
     * @param secret         密钥
     * @param algorithm      签名算法
     * @return 是否验证通过
     */
    public static boolean verify(Map<String, String> params, String signature, String secret, SignatureAlgorithm algorithm) {
        String expectedSign = sign(params, secret, algorithm);
        return StrUtil.equalsIgnoreCase(expectedSign, signature);
    }
    
    /**
     * 验证签名（带 body）
     *
     * @param params    请求参数
     * @param body      请求体
     * @param signature 请求签名
     * @param secret    密钥
     * @param algorithm 签名算法
     * @return 是否验证通过
     */
    public static boolean verify(Map<String, String> params, String body, String signature, String secret, SignatureAlgorithm algorithm) {
        String expectedSign = sign(params, body, secret, algorithm);
        return StrUtil.equalsIgnoreCase(expectedSign, signature);
    }
    
    /**
     * 生成 Nonce
     *
     * @return 随机字符串
     */
    public static String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 获取当前时间戳（秒）
     *
     * @return 时间戳
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }
    
    /**
     * 检查时间戳是否有效
     *
     * @param timestamp        请求时间戳（秒）
     * @param toleranceSeconds 容差（秒）
     * @return 是否有效
     */
    public static boolean isTimestampValid(long timestamp, long toleranceSeconds) {
        long now = getCurrentTimestamp();
        return Math.abs(now - timestamp) <= toleranceSeconds;
    }
}
