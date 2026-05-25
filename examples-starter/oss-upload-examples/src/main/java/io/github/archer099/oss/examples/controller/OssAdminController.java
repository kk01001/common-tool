package io.github.archer099.oss.examples.controller;

import cn.hutool.core.util.StrUtil;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import io.github.archer099.common.model.ApiResponse;
import io.github.archer099.oss.OssProperties;
import io.github.archer099.oss.client.OssClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.time.Instant;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author linshiqiang
 * @date 2025-11-17 10:58:30
 * @description OSS 管理示例：创建桶、设置桶 ACL、验证 AK/SK
 */
@RestController
@RequestMapping("/api/oss/admin")
@RequiredArgsConstructor
@Slf4j
public class OssAdminController {

    private final OssClient ossClient;
    private final OssProperties ossProperties;
    private static final long APP_START_TS = System.currentTimeMillis();

    @PostMapping("/createBucket")
    public ApiResponse<Void> createBucket(@RequestBody Map<String, String> req) {
        String bucketName = req.get("bucketName");
        if (StrUtil.isBlank(bucketName)) {
            return ApiResponse.fail(400);
        }
        ossClient.createBucket(bucketName);
        return ApiResponse.ok();
    }

    @PostMapping("/listBuckets")
    public ApiResponse<List<Map<String, String>>> listBuckets() {
        List<Bucket> buckets = ossClient.getAllBuckets();
        List<Map<String, String>> list = buckets.stream()
                .map(b -> Map.of("name", b.getName()))
                .collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    @PostMapping("/deleteBucket")
    public ApiResponse<Void> deleteBucket(@RequestBody Map<String, String> req) {
        String bucketName = req.get("bucketName");
        if (StrUtil.isBlank(bucketName)) {
            return ApiResponse.fail(400);
        }
        ossClient.removeBucket(bucketName);
        return ApiResponse.ok();
    }

    @PostMapping("/setBucketAcl")
    public ApiResponse<Void> setBucketAcl(@RequestBody Map<String, String> req) {
        String bucketName = StrUtil.blankToDefault(req.get("bucketName"), ossProperties.getBucketName());
        String acl = req.get("acl");
        if (StrUtil.isBlank(bucketName) || StrUtil.isBlank(acl)) {
            return ApiResponse.fail(400);
        }

        String v = acl.trim().toLowerCase();
        try {
            // 兼容 MinIO 等不支持 ACL 的实现：改用 Bucket Policy 实现公开/私有访问
            switch (v) {
                case "private":
                    // 删除策略，回退为默认私有
                    ossClient.getS3Client().deleteBucketPolicy(bucketName);
                    log.info("Set bucket PRIVATE via bucket policy delete, bucket={}", bucketName);
                    break;
                case "public-read":
                    String readPolicy = genPublicReadPolicy(bucketName);
                    ossClient.getS3Client().setBucketPolicy(bucketName, readPolicy);
                    log.info("Set bucket PUBLIC-READ via bucket policy, bucket={}", bucketName);
                    break;
                case "public-read-write":
                    String rwPolicy = genPublicReadWritePolicy(bucketName);
                    ossClient.getS3Client().setBucketPolicy(bucketName, rwPolicy);
                    log.info("Set bucket PUBLIC-READ-WRITE via bucket policy, bucket={}", bucketName);
                    break;
                default:
                    return ApiResponse.failOfMessage("不支持的 ACL：" + acl, 400);
            }
            return ApiResponse.ok();
        } catch (Exception e) {
            log.warn("Set bucket ACL(policy) failed, bucket={}, acl={}, err={}", bucketName, acl, e.getMessage());
            return ApiResponse.failOfMessage("设置失败：" + e.getMessage(), 400);
        }
    }

    private String genPublicReadPolicy(String bucketName) {
        // 允许所有人读取该桶中的对象
        return "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Sid\": \"PublicReadGetObject\",\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": \"*\",\n" +
                "      \"Action\": [\"s3:GetObject\"],\n" +
                "      \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    private String genPublicReadWritePolicy(String bucketName) {
        // 允许所有人读写（风险高，仅用于示例）
        return "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Sid\": \"PublicReadWriteObjects\",\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": \"*\",\n" +
                "      \"Action\": [\"s3:GetObject\", \"s3:PutObject\", \"s3:DeleteObject\"],\n" +
                "      \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    @PostMapping("/validateCredentials")
    public ApiResponse<Map<String, Object>> validateCredentials(@RequestBody Map<String, Object> req) {
        String endpoint = (String) req.get("endpoint");
        String region = (String) req.get("region");
        String accessKey = (String) req.get("accessKey");
        String accessSecret = (String) req.get("accessSecret");
        Boolean pathStyleAccess = (Boolean) req.getOrDefault("pathStyleAccess", Boolean.TRUE);
        Boolean chunkedDisabled = (Boolean) req.getOrDefault("chunkedEncodingDisabled", Boolean.FALSE);
        if (StrUtil.hasBlank(endpoint, region, accessKey, accessSecret)) {
            return ApiResponse.fail(400);
        }
        try {
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setMaxConnections(ossProperties.getMaxConnections());

            AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, accessSecret);
            AmazonS3Client builder = (AmazonS3Client) AmazonS3Client.builder()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withClientConfiguration(clientConfiguration)
                    .withPathStyleAccessEnabled(Boolean.TRUE.equals(pathStyleAccess))
                    .build();
            if (Boolean.TRUE.equals(chunkedDisabled)) {
                com.amazonaws.services.s3.S3ClientOptions opts =
                        com.amazonaws.services.s3.S3ClientOptions.builder()
                                .disableChunkedEncoding()
                                .build();
                builder.setS3ClientOptions(opts);
            }
            List<Bucket> buckets = builder.listBuckets();
            return ApiResponse.ok(Map.of(
                    "success", true,
                    "bucketCount", buckets.size()
            ));
        } catch (Exception e) {
            log.warn("Validate credentials failed: {}", e.getMessage());
            return ApiResponse.ok(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    private CannedAccessControlList parseAcl(String acl) {
        String v = acl.trim().toLowerCase();
        return switch (v) {
            case "private" -> CannedAccessControlList.Private;
            case "public-read" -> CannedAccessControlList.PublicRead;
            case "public-read-write" -> CannedAccessControlList.PublicReadWrite;
            case "authenticated-read" -> CannedAccessControlList.AuthenticatedRead;
            case "log-delivery-write" -> CannedAccessControlList.LogDeliveryWrite;
            case "bucket-owner-read" -> CannedAccessControlList.BucketOwnerRead;
            case "bucket-owner-full-control" -> CannedAccessControlList.BucketOwnerFullControl;
            default -> null;
        };
    }

    /**
     * 指标汇总：桶数量、对象数量、总占用字节、运行信息
     */
    @PostMapping("/metrics")
    public ApiResponse<Map<String, Object>> metrics(@RequestBody(required = false) Map<String, Object> req) {
        String bucket = StrUtil.blankToDefault(req == null ? null : (String) req.get("bucketName"), ossProperties.getBucketName());
        List<Bucket> buckets = ossClient.getAllBuckets();
        int bucketCount = buckets == null ? 0 : buckets.size();

        long objectCount = 0L;
        long usageBytes = 0L;
        // 优先使用 MinIO Prometheus v3 端点聚合（零遍历），失败再降级
        BucketStats stats = fetchBucketStatsFromPrometheus(bucket);
        if (stats != null) {
            objectCount = stats.objects;
            usageBytes = stats.bytes;
        } else {
            // v2 或 Prom 端点不可用时，回退为遍历
            try {
                if (StrUtil.isNotBlank(bucket)) {
                    ListObjectsV2Request r = new ListObjectsV2Request().withBucketName(bucket).withMaxKeys(1000);
                    ListObjectsV2Result res;
                    do {
                        res = ossClient.getS3Client().listObjectsV2(r);
                        for (S3ObjectSummary s : res.getObjectSummaries()) {
                            objectCount++;
                            usageBytes += s.getSize();
                        }
                        r.setContinuationToken(res.getNextContinuationToken());
                    } while (res.isTruncated());
                }
            } catch (Exception e) {
                log.warn("metrics scan failed, bucket={}, err={}", bucket, e.getMessage());
            }
        }

        Map<String, Object> data = Map.of(
                "bucketCount", bucketCount,
                "objectCount", objectCount,
                "usageBytes", usageBytes,
                "endpoint", ossProperties.getEndpoint(),
                "region", ossProperties.getRegion(),
                "serverTime", Instant.now().toString(),
                "uptimeMs", System.currentTimeMillis() - APP_START_TS,
                "bucketName", bucket
        );
        return ApiResponse.ok(data);
    }

    /**
     * 通过 MinIO Prometheus v3/v2 指标端点获取 bucket 的对象数与占用字节（若可用）。
     * 优先：/minio/metrics/v3/cluster/usage/buckets （支持 JSON/Markdown 表）
     * 备选：/minio/v2/metrics/bucket （Prometheus 文本）
     */
    private BucketStats fetchBucketStatsFromPrometheus(String bucket) {
        try {
            if (StrUtil.isBlank(bucket)) {
                return null;
            }
            String base = StrUtil.removeSuffix(ossProperties.getEndpoint(), "/");
            // v3 用法：优先尝试 JSON，失败再尝试 Markdown 表
            String v3Url = base + "/minio/metrics/v3/cluster/usage/buckets";
            BucketStats s = tryFetchV3Json(v3Url, bucket);
            if (s != null) {
                return s;
            }
            s = tryFetchV3Markdown(v3Url, bucket);
            if (s != null) {
                return s;
            }
            // v2 文本
            String v2Url = base + "/minio/v2/metrics/bucket";
            s = tryFetchV2Text(v2Url, bucket);
            return s;
        } catch (Exception e) {
            log.debug("fetchBucketStatsFromPrometheus error: {}", e.getMessage());
            return null;
        }
    }

    private BucketStats tryFetchV3Json(String url, String bucket) {
        try {
            HttpResponse<String> resp = httpGet(url, "application/json");
            if (resp == null || resp.statusCode() != 200 || StrUtil.isBlank(resp.body())) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(resp.body());
            // 兼容不同结构：尝试 rows/list/array
            JsonNode arr = null;
            if (root.isArray()) {
                arr = root;
            } else if (root.has("rows")) {
                arr = root.get("rows");
            } else if (root.has("data")) {
                arr = root.get("data");
            }
            if (arr == null || !arr.isArray()) {
                return null;
            }
            long objects = -1L;
            long bytes = -1L;
            for (JsonNode item : arr) {
                // 查找 bucket 字段或 labels.bucket
                String b = null;
                if (item.has("bucket")) {
                    b = item.get("bucket").asText(null);
                }
                if (StrUtil.isBlank(b) && item.has("labels") && item.get("labels").has("bucket")) {
                    b = item.get("labels").get("bucket").asText(null);
                }
                if (!StrUtil.equals(b, bucket)) {
                    continue;
                }
                // 搜索对象数与容量字段
                // 常见字段：objects / objectCount / totalObjects
                if (item.has("objects")) {
                    objects = safeLong(item.get("objects"));
                } else if (item.has("objectCount")) {
                    objects = safeLong(item.get("objectCount"));
                } else if (item.has("totalObjects")) {
                    objects = safeLong(item.get("totalObjects"));
                }
                // 常见字段：size / usageBytes / totalBytes / bytes
                if (item.has("size")) {
                    bytes = safeLong(item.get("size"));
                } else if (item.has("usageBytes")) {
                    bytes = safeLong(item.get("usageBytes"));
                } else if (item.has("totalBytes")) {
                    bytes = safeLong(item.get("totalBytes"));
                } else if (item.has("bytes")) {
                    bytes = safeLong(item.get("bytes"));
                }
                if (objects >= 0 && bytes >= 0) {
                    return new BucketStats(objects, bytes);
                }
            }
            return (objects >= 0 || bytes >= 0) ? new BucketStats(Math.max(objects, 0), Math.max(bytes, 0)) : null;
        } catch (Exception e) {
            log.debug("tryFetchV3Json parse error: {}", e.getMessage());
            return null;
        }
    }

    private BucketStats tryFetchV3Markdown(String url, String bucket) {
        try {
            HttpResponse<String> resp = httpGet(url, "text/plain");
            if (resp == null || resp.statusCode() != 200 || StrUtil.isBlank(resp.body())) {
                return null;
            }
            String body = resp.body();
            // Markdown 表：第一行为表头，例如 | Bucket | Objects | Size | ...
            String[] lines = body.split("\r?\n");
            int headerIdx = -1;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains("|") && lines[i].toLowerCase().contains("bucket") && lines[i].toLowerCase().contains("object")) {
                    headerIdx = i;
                    break;
                }
            }
            if (headerIdx < 0) {
                return null;
            }
            String[] headers = splitTableLine(lines[headerIdx]);
            int bucketCol = indexOf(headers, "bucket");
            int objectsCol = indexOf(headers, "objects");
            int sizeCol = indexOf(headers, "size");
            int bytesCol = indexOf(headers, "bytes");
            for (int i = headerIdx + 1; i < lines.length; i++) {
                if (!lines[i].contains("|")) continue;
                String[] cols = splitTableLine(lines[i]);
                if (bucketCol >= 0 && bucketCol < cols.length && StrUtil.equals(cols[bucketCol].trim(), bucket)) {
                    long objects = parseLongSafe(objectsCol >= 0 && objectsCol < cols.length ? cols[objectsCol] : "0");
                    long size = parseSizeToBytes(sizeCol >= 0 && sizeCol < cols.length ? cols[sizeCol] : cols[Math.max(bytesCol, 0)]);
                    return new BucketStats(objects, size);
                }
            }
            return null;
        } catch (Exception e) {
            log.debug("tryFetchV3Markdown parse error: {}", e.getMessage());
            return null;
        }
    }

    private BucketStats tryFetchV2Text(String url, String bucket) {
        try {
            HttpResponse<String> resp = httpGet(url, "text/plain");
            if (resp == null || resp.statusCode() != 200 || StrUtil.isBlank(resp.body())) {
                return null;
            }
            String body = resp.body();
            long objects = -1L;
            long bytes = -1L;
            // 解析 Prometheus 文本：metric{bucket="xxx"} value
            String[] lines = body.split("\r?\n");
            for (String line : lines) {
                if (!line.contains("bucket=\"" + bucket + "\"")) continue;
                String trimmed = line.trim();
                if (trimmed.startsWith("#")) continue;
                int space = trimmed.lastIndexOf(' ');
                if (space <= 0) continue;
                String valueStr = trimmed.substring(space + 1).trim();
                long value = parseLongSafe(valueStr);
                // 尝试匹配常见指标名
                if (trimmed.startsWith("minio_bucket_object_count") || trimmed.startsWith("bucket_objects_count")
                        || trimmed.startsWith("bucket_objects_total") || trimmed.startsWith("bucket_objects")) {
                    objects = value;
                }
                if (trimmed.startsWith("minio_bucket_size") || trimmed.startsWith("bucket_usage_total_bytes")
                        || trimmed.startsWith("bucket_total_bytes") || trimmed.startsWith("bucket_usage_bytes")) {
                    bytes = value;
                }
            }
            return (objects >= 0 || bytes >= 0) ? new BucketStats(Math.max(objects, 0), Math.max(bytes, 0)) : null;
        } catch (Exception e) {
            log.debug("tryFetchV2Text parse error: {}", e.getMessage());
            return null;
        }
    }

    private HttpResponse<String> httpGet(String url, String accept) {
        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).GET()
                    .header("Accept", accept);
            // 处理 MinIO 指标鉴权：public 或 bearer
            String authType = System.getenv("MINIO_PROMETHEUS_AUTH_TYPE");
            if (StrUtil.equalsIgnoreCase(authType, "bearer")) {
                String token = System.getenv("MINIO_PROMETHEUS_BEARER_TOKEN");
                if (StrUtil.isNotBlank(token)) {
                    builder.header("Authorization", "Bearer " + token);
                }
            }
            HttpRequest request = builder.build();
            return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.debug("httpGet {} failed: {}", url, e.getMessage());
            return null;
        }
    }

    private String[] splitTableLine(String line) {
        // 去除首尾竖线并按竖线分列
        String s = line.trim();
        if (s.startsWith("|") && s.endsWith("|")) {
            s = s.substring(1, s.length() - 1);
        }
        String[] cols = s.split("\\|");
        for (int i = 0; i < cols.length; i++) {
            cols[i] = cols[i].trim();
        }
        return cols;
    }

    private int indexOf(String[] headers, String key) {
        String k = key.toLowerCase();
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].toLowerCase();
            if (h.contains(k)) return i;
        }
        return -1;
    }

    private long parseLongSafe(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    private long parseSizeToBytes(String s) {
        if (StrUtil.isBlank(s)) return 0L;
        String str = s.trim().toLowerCase();
        try {
            if (str.endsWith("gib")) {
                return (long) (Double.parseDouble(str.replace("gib", "").trim()) * 1024 * 1024 * 1024);
            } else if (str.endsWith("mib")) {
                return (long) (Double.parseDouble(str.replace("mib", "").trim()) * 1024 * 1024);
            } else if (str.endsWith("kib")) {
                return (long) (Double.parseDouble(str.replace("kib", "").trim()) * 1024);
            } else if (str.endsWith("b")) {
                return (long) (Double.parseDouble(str.replace("b", "").trim()));
            }
        } catch (Exception ignored) {
        }
        // 纯数字当 bytes
        return parseLongSafe(s);
    }

    private long safeLong(JsonNode n) {
        if (n == null) return 0L;
        if (n.isLong() || n.isInt()) return n.asLong();
        if (n.isTextual()) return parseLongSafe(n.asText());
        if (n.isDouble() || n.isFloat()) return (long) n.asDouble();
        return 0L;
    }

    private static class BucketStats {
        final long objects;
        final long bytes;
        BucketStats(long objects, long bytes) {
            this.objects = objects;
            this.bytes = bytes;
        }
    }
}