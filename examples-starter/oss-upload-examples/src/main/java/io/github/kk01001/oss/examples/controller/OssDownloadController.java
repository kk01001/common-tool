package io.github.archer099.oss.examples.controller;

import cn.hutool.core.util.StrUtil;
import io.github.archer099.common.model.ApiResponse;
import io.github.archer099.oss.OssProperties;
import io.github.archer099.oss.client.OssClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * @author linshiqiang
 * @date 2025-11-17 10:45:30
 * @description OSS 下载示例接口
 */
@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
@Slf4j
public class OssDownloadController {

    private final OssClient ossClient;
    private final OssProperties ossProperties;

    /**
     * 生成对象的临时下载链接（预签名 URL）
     *
     * 请求示例：
     * {
     *   "bucketName": "your-bucket(optional)",
     *   "objectName": "path/to/file.ext",
     *   "expiresSeconds": 900
     * }
     */
    @PostMapping("/downloadUrl")
    public ApiResponse<Map<String, String>> downloadUrl(@RequestBody Map<String, Object> req) {
        String bucket = StrUtil.blankToDefault((String) req.get("bucketName"), ossProperties.getBucketName());
        String object = (String) req.get("objectName");
        Number expires = (Number) req.getOrDefault("expiresSeconds", 900);
        if (StrUtil.isBlank(object)) {
            return ApiResponse.fail(400);
        }
        long seconds = Objects.isNull(expires) ? 900L : expires.longValue();
        if (seconds <= 0) {
            seconds = 900L;
        }
        String url = ossClient.getObjectUrl(bucket, object, Duration.ofSeconds(seconds));
        log.info("Generate download url, bucket={}, object={}, expiresSeconds={}", bucket, object, seconds);
        return ApiResponse.ok(Map.of("url", url));
    }

    /**
     * 获取对象元数据（大小、类型等）
     *
     * 请求示例：
     * {
     *   "bucketName": "your-bucket(optional)",
     *   "objectName": "path/to/file.ext"
     * }
     */
    @PostMapping("/metadata")
    public ApiResponse<Map<String, Object>> metadata(@RequestBody Map<String, String> req) {
        String bucket = StrUtil.blankToDefault(req.get("bucketName"), ossProperties.getBucketName());
        String object = req.get("objectName");
        if (StrUtil.isBlank(object)) {
            return ApiResponse.fail(400);
        }
        var meta = ossClient.getObjectMetadata(bucket, object);
        return ApiResponse.ok(Map.of(
                "contentLength", meta.getContentLength(),
                "contentType", meta.getContentType(),
                "etag", meta.getETag(),
                "lastModified", String.valueOf(meta.getLastModified())
        ));
    }
}