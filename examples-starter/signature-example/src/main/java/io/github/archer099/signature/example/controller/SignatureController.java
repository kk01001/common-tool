package io.github.archer099.signature.example.controller;

import io.github.archer099.signature.annotation.IgnoreSignature;
import io.github.archer099.signature.annotation.SignatureVerify;
import io.github.archer099.signature.core.SignatureAlgorithm;
import io.github.archer099.signature.example.dto.ApiRequest;
import io.github.archer099.signature.example.dto.ApiResponse;
import io.github.archer099.signature.example.dto.NestedRequest;
import io.github.archer099.signature.handler.SignatureHandler;
import io.github.archer099.signature.util.SignatureUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description API签名验证示例控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SignatureVerify  // 类级别启用签名验证
public class SignatureController {

    private final SignatureHandler signatureHandler;

    /**
     * 需要签名验证的接口
     */
    @GetMapping("/secure/data")
    public ApiResponse<Map<String, Object>> getSecureData(
            @RequestParam(required = false) String param1,
            @RequestParam(required = false) String param2,
            HttpServletRequest request) {
        
        String appKey = (String) request.getAttribute("signatureAppKey");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "签名验证通过，获取安全数据成功");
        data.put("appKey", appKey);
        data.put("param1", param1);
        data.put("param2", param2);
        data.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.success(data);
    }

    /**
     * 需要签名验证的POST接口
     */
    @PostMapping("/secure/submit")
    public ApiResponse<Map<String, Object>> submitSecureData(@RequestBody ApiRequest request,
                                                              HttpServletRequest httpRequest) {
        String appKey = (String) httpRequest.getAttribute("signatureAppKey");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "签名验证通过，数据提交成功");
        data.put("appKey", appKey);
        data.put("receivedData", request);
        data.put("processTime", System.currentTimeMillis());
        
        return ApiResponse.success(data);
    }

    /**
     * 需要验证请求体的接口（使用 DTO 对象接收）
     * 签名验证时会验证原始 JSON 字符串（parseJsonBody=true 解析字段参与排序）
     */
    @PostMapping("/secure/submitWithBody")
    @SignatureVerify(verifyBody = true, parseJsonBody = true)
    public ApiResponse<Map<String, Object>> submitWithBody(@RequestBody ApiRequest request,
                                                           HttpServletRequest httpRequest) {
        String appKey = (String) httpRequest.getAttribute("signatureAppKey");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "签名验证通过（含请求体），数据提交成功");
        data.put("appKey", appKey);
        data.put("receivedData", request);
        data.put("userId", request.getUserId());
        data.put("action", request.getAction());
        
        return ApiResponse.success(data);
    }

    /**
     * 需要验证请求体的接口（多级嵌套JSON）
     * parseJsonBody=true 将 JSON 字段解析后参与签名排序
     */
    @PostMapping("/secure/submitNested")
    @SignatureVerify(verifyBody = true, parseJsonBody = true)
    public ApiResponse<Map<String, Object>> submitNested(@RequestBody NestedRequest request,
                                                          HttpServletRequest httpRequest) {
        String appKey = (String) httpRequest.getAttribute("signatureAppKey");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "签名验证通过（多级嵌套JSON），数据提交成功");
        data.put("appKey", appKey);
        data.put("orderId", request.getOrderId());
        data.put("user", request.getUser());
        data.put("address", request.getAddress());
        data.put("products", request.getProducts());
        data.put("totalAmount", request.getTotalAmount());
        
        return ApiResponse.success(data);
    }

    /**
     * 公开接口（忽略签名验证）
     */
    @GetMapping("/public/info")
    @IgnoreSignature
    public ApiResponse<Map<String, Object>> getPublicInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是公开接口，无需签名验证");
        data.put("version", "1.0.0");
        data.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.success(data);
    }

    /**
     * 生成签名（用于测试）
     */
    @PostMapping("/tool/sign")
    @IgnoreSignature
    public ApiResponse<Map<String, Object>> generateSign(@RequestBody Map<String, String> params) {
        String appKey = params.get("appKey");
        String appSecret = params.get("appSecret");
        
        if (appKey == null || appSecret == null) {
            return ApiResponse.fail("PARAM_ERROR", "缺少 appKey 或 appSecret");
        }
        
        // 生成时间戳和 nonce
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        
        // 构建签名参数
        TreeMap<String, String> signParams = new TreeMap<>();
        signParams.put("appKey", appKey);
        signParams.put("timestamp", timestamp);
        signParams.put("nonce", nonce);
        
        // 添加其他参数
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!"appKey".equals(entry.getKey()) && !"appSecret".equals(entry.getKey())) {
                signParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        // 生成签名
        String signature = SignatureUtil.sign(signParams, appSecret, SignatureAlgorithm.HMAC_SHA256);
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("appKey", appKey);
        result.put("timestamp", timestamp);
        result.put("nonce", nonce);
        result.put("signature", signature);
        result.put("signContent", SignatureUtil.buildSignContent(signParams) + "&secret=" + appSecret);
        result.put("headers", Map.of(
                "X-App-Key", appKey,
                "X-Signature", signature,
                "X-Timestamp", timestamp,
                "X-Nonce", nonce
        ));
        
        return ApiResponse.success("签名生成成功", result);
    }

    /**
     * 生成签名（包含请求体，用于测试）
     * 使用 JSON body 字段解析方式
     */
    @PostMapping("/tool/signWithBody")
    @IgnoreSignature
    public ApiResponse<Map<String, Object>> generateSignWithBody(@RequestBody Map<String, String> params) {
        String appKey = params.get("appKey");
        String appSecret = params.get("appSecret");
        String body = params.get("body");
        
        if (appKey == null || appSecret == null) {
            return ApiResponse.fail("PARAM_ERROR", "缺少 appKey 或 appSecret");
        }
        
        // 生成时间戳和 nonce
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        
        // 构建签名参数
        TreeMap<String, String> signParams = new TreeMap<>();
        signParams.put("appKey", appKey);
        signParams.put("timestamp", timestamp);
        signParams.put("nonce", nonce);
        
        // 生成签名（JSON body 字段参与排序）
        String signature = SignatureUtil.signWithJsonBody(signParams, body, appSecret, 
                SignatureAlgorithm.HMAC_SHA256, "body.");
        
        // 构建签名内容用于调试
        TreeMap<String, String> allParams = new TreeMap<>(signParams);
        try {
            cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(body);
            flattenJson(json, "body.", allParams);
        } catch (Exception e) {
            allParams.put("body", body);
        }
        String signContent = SignatureUtil.buildSignContent(allParams) + "&secret=" + appSecret;
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("appKey", appKey);
        result.put("timestamp", timestamp);
        result.put("nonce", nonce);
        result.put("signature", signature);
        result.put("parseJsonBody", true);
        result.put("signContent", signContent);
        result.put("headers", Map.of(
                "X-App-Key", appKey,
                "X-Signature", signature,
                "X-Timestamp", timestamp,
                "X-Nonce", nonce
        ));
        
        return ApiResponse.success("签名生成成功（JSON字段参与排序）", result);
    }
    
    /**
     * 将 JSON 对象扁平化
     */
    private void flattenJson(cn.hutool.json.JSONObject json, String prefix, Map<String, String> result) {
        for (String key : json.keySet()) {
            Object value = json.get(key);
            String fullKey = cn.hutool.core.util.StrUtil.isBlank(prefix) ? key : prefix + key;
            
            if (value instanceof cn.hutool.json.JSONObject nestedJson) {
                flattenJson(nestedJson, fullKey + ".", result);
            } else if (value instanceof cn.hutool.json.JSONArray jsonArray) {
                result.put(fullKey, jsonArray.toString());
            } else if (value != null) {
                result.put(fullKey, value.toString());
            }
        }
    }

    /**
     * 验证签名（用于测试）
     */
    @PostMapping("/tool/verify")
    @IgnoreSignature
    public ApiResponse<Map<String, Object>> verifySign(@RequestBody Map<String, String> params) {
        String appKey = params.get("appKey");
        String signature = params.get("signature");
        String timestamp = params.get("timestamp");
        String nonce = params.get("nonce");
        
        // 移除签名相关参数，保留业务参数
        Map<String, String> signParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!"signature".equals(entry.getKey()) && !"appSecret".equals(entry.getKey())) {
                signParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        var result = signatureHandler.verify(appKey, signature, timestamp, nonce, signParams, null);
        
        Map<String, Object> data = new HashMap<>();
        data.put("valid", result.isValid());
        data.put("errorCode", result.getErrorCode());
        data.put("errorMessage", result.getErrorMessage());
        data.put("expectedSignature", result.getExpectedSignature());
        data.put("receivedSignature", signature);
        
        if (result.isValid()) {
            return ApiResponse.success("签名验证通过", data);
        } else {
            return ApiResponse.fail(result.getErrorCode(), result.getErrorMessage());
        }
    }
}
