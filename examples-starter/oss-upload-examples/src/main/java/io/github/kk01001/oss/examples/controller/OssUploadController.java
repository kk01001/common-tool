package io.github.archer099.oss.examples.controller;

import cn.hutool.core.util.StrUtil;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartResult;
import io.github.archer099.common.model.ApiResponse;
import io.github.archer099.oss.OssProperties;
import io.github.archer099.oss.client.OssClient;
import io.github.archer099.oss.model.ChunkDTO;
import io.github.archer099.oss.model.ChunkMergeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author linshiqiang
 * @date 2025-11-14 10:26:31
 * @description OSS 分片上传示例接口
 */
@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
@Slf4j
public class OssUploadController {

    private final OssClient ossClient;
    private final OssProperties ossProperties;

    @PostMapping("/initiate")
    public ApiResponse<Map<String, String>> initiate(@RequestBody Map<String, String> req) {
        String bucket = StrUtil.blankToDefault(req.get("bucketName"), ossProperties.getBucketName());
        String object = req.get("objectName");
        if (StrUtil.isBlank(object)) {
            return ApiResponse.fail(400);
        }
        InitiateMultipartUploadResult res = ossClient.initiateMultipartUpload(bucket, object);
        return ApiResponse.ok(Map.of("uploadId", res.getUploadId()));
    }

    @PostMapping("/uploadPart")
    public ApiResponse<Map<String, Object>> uploadPart(@RequestParam("uploadId") String uploadId,
                                                       @RequestParam(value = "bucketName", required = false) String bucketName,
                                                       @RequestParam("objectName") String objectName,
                                                       @RequestParam("chunkNumber") Integer chunkNumber,
                                                       @RequestParam(value = "isLastPart", required = false) Boolean isLastPart,
                                                       @RequestPart("file") MultipartFile file) throws Exception {
        if (StrUtil.hasBlank(uploadId, objectName) || Objects.isNull(file)) {
            return ApiResponse.fail(400);
        }
        String bucket = StrUtil.blankToDefault(bucketName, ossProperties.getBucketName());
        ChunkDTO chunkDTO = new ChunkDTO();
        chunkDTO.setUploadId(uploadId);
        chunkDTO.setBucketName(bucket);
        chunkDTO.setObjectName(objectName);
        chunkDTO.setChunkNumber(chunkNumber);
        chunkDTO.setIsLastPart(Boolean.TRUE.equals(isLastPart));
        chunkDTO.setCurrentChunkSize(file.getSize());
        chunkDTO.setFile(file);
        long start = System.currentTimeMillis();
        UploadPartResult res = ossClient.uploadPart(chunkDTO);
        PartETag etag = res.getPartETag();
        log.info("uploadPart cost: {}ms", System.currentTimeMillis() - start);
        return ApiResponse.ok(Map.of(
                "partNumber", etag.getPartNumber(),
                "eTag", etag.getETag()
        ));
    }

    @PostMapping("/complete")
    public ApiResponse<Map<String, String>> complete(@RequestBody Map<String, Object> req) {
        String uploadId = (String) req.get("uploadId");
        String bucketName = ossProperties.getBucketName();
        String objectName = (String) req.get("objectName");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parts = (List<Map<String, Object>>) req.get("parts");

        if (StrUtil.hasBlank(uploadId, objectName) || Objects.isNull(parts) || parts.isEmpty()) {
            return ApiResponse.fail(400);
        }

        List<PartETag> etags = parts.stream()
                .map(p -> new PartETag(((Number) p.get("partNumber")).intValue(), (String) p.get("eTag")))
                .toList();

        ChunkMergeDTO mergeDTO = new ChunkMergeDTO();
        mergeDTO.setBucketName(bucketName);
        mergeDTO.setObjectName(objectName);
        mergeDTO.setUploadId(uploadId);
        mergeDTO.setChunkList(etags);
        CompleteMultipartUploadResult res = ossClient.completeMultipartUpload(mergeDTO);
        return ApiResponse.ok(Map.of("location", String.valueOf(res.getLocation())));
    }

    @PostMapping("/listParts")
    public ApiResponse<Map<String, Object>> listParts(@RequestBody Map<String, String> req) {
        String bucket = StrUtil.blankToDefault(req.get("bucketName"), ossProperties.getBucketName());
        String object = req.get("objectName");
        String uploadId = req.get("uploadId");
        if (StrUtil.hasBlank(object, uploadId)) {
            return ApiResponse.fail(400);
        }
        var listing = ossClient.listParts(bucket, object, uploadId);
        var parts = listing.getParts().stream()
                .map(p -> Map.of(
                        "partNumber", p.getPartNumber(),
                        "eTag", p.getETag(),
                        "size", p.getSize()
                ))
                .toList();
        return ApiResponse.ok(Map.of(
                "isTruncated", listing.isTruncated(),
                "nextPartNumberMarker", listing.getNextPartNumberMarker(),
                "maxParts", listing.getMaxParts(),
                "parts", parts
        ));
    }

    @PostMapping("/abort")
    public ApiResponse<Void> abort(@RequestBody Map<String, String> req) {
        String bucket = StrUtil.blankToDefault(req.get("bucketName"), ossProperties.getBucketName());
        String object = req.get("objectName");
        String uploadId = req.get("uploadId");
        if (StrUtil.hasBlank(object, uploadId)) {
            return ApiResponse.fail(400);
        }
        ossClient.abortMultipartUpload(bucket, object, uploadId);
        return ApiResponse.ok();
    }

    @PostMapping("/putObject")
    public ApiResponse<Map<String, String>> putObject(@RequestParam(value = "bucketName", required = false) String bucketName,
                                                      @RequestParam("objectName") String objectName,
                                                      @RequestPart("file") MultipartFile file) throws Exception {
        if (StrUtil.isBlank(objectName) || Objects.isNull(file)) {
            return ApiResponse.fail(400);
        }
        String bucket = StrUtil.blankToDefault(bucketName, ossProperties.getBucketName());
        long start = System.currentTimeMillis();
        var res = ossClient.putObject(bucket, objectName, file.getInputStream());
        log.info("putObject cost: {}ms", System.currentTimeMillis() - start);
        return ApiResponse.ok(Map.of("eTag", res.getETag()));
    }
}