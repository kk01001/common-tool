package io.github.kk01001.signature.properties;

import io.github.kk01001.signature.core.SignatureAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kk01001
 * @date 2026-01-18 13:23:23
 * @description 签名配置属性
 */
@Data
@ConfigurationProperties(prefix = "signature")
public class SignatureProperties {
    
    /**
     * 是否启用签名验证
     */
    private boolean enabled = true;
    
    /**
     * 签名算法
     */
    private SignatureAlgorithm algorithm = SignatureAlgorithm.HMAC_SHA256;
    
    /**
     * 时间戳有效期（防止请求过期）
     */
    private Duration timestampTolerance = Duration.ofMinutes(5);
    
    /**
     * Nonce 有效期（防重放窗口）
     */
    private Duration nonceTtl = Duration.ofMinutes(10);
    
    /**
     * 是否启用 Nonce 验证（防重放）
     */
    private boolean nonceEnabled = true;
    
    /**
     * 是否启用时间戳验证
     */
    private boolean timestampEnabled = true;
    
    /**
     * AppKey 请求头/参数名
     */
    private String appKeyHeader = "X-App-Key";
    
    /**
     * 签名请求头/参数名
     */
    private String signatureHeader = "X-Signature";
    
    /**
     * 时间戳请求头/参数名
     */
    private String timestampHeader = "X-Timestamp";
    
    /**
     * Nonce 请求头/参数名
     */
    private String nonceHeader = "X-Nonce";
    
    /**
     * 是否从请求参数获取签名信息（如果请求头中没有）
     */
    private boolean allowQueryParams = true;
    
    /**
     * 签名参数在请求参数中的名称
     */
    private String appKeyParam = "appKey";
    private String signatureParam = "sign";
    private String timestampParam = "timestamp";
    private String nonceParam = "nonce";
    
    /**
     * 排除的 URL 路径（不进行签名验证）
     */
    private List<String> excludePaths = new ArrayList<>();
    
    /**
     * 需要验证的 URL 路径（如果为空，则验证所有路径）
     */
    private List<String> includePaths = new ArrayList<>();
    
    /**
     * 简单模式：使用配置的 AppKey 和 AppSecret（适合单应用场景）
     */
    private Map<String, String> apps = new HashMap<>();
    
    /**
     * 是否开启调试模式（输出签名计算过程）
     */
    private boolean debug = false;
    
    /**
     * 是否将 JSON Body 字段解析后参与签名排序
     * true: JSON 字段解析后与其他参数合并排序签名（推荐，字段级别校验）
     * false: JSON Body 作为整体字符串参与签名（原始方式）
     */
    private boolean parseJsonBody = true;
    
    /**
     * JSON Body 字段前缀（仅 parseJsonBody=true 时生效）
     * 如设置为 "body."，则 JSON 中的 userId 字段签名时为 body.userId
     */
    private String jsonBodyPrefix = "body.";
    
    /**
     * 签名失败后的响应配置
     */
    private Response response = new Response();
    
    @Data
    public static class Response {
        /**
         * HTTP 状态码
         */
        private int status = 401;
        
        /**
         * Content-Type
         */
        private String contentType = "application/json;charset=UTF-8";
        
        /**
         * 响应体模板（支持变量：${errorCode}、${errorMessage}）
         */
        private String body = "{\"code\":\"${errorCode}\",\"message\":\"${errorMessage}\"}";
    }
}
