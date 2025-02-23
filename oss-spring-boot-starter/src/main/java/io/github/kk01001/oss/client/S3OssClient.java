package io.github.kk01001.oss.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.util.IOUtils;
import io.github.kk01001.oss.listener.CustomProgressListener;
import io.github.kk01001.oss.listener.ProgressListenerAdapter;
import io.github.kk01001.oss.model.ChunkDTO;
import io.github.kk01001.oss.model.ChunkMergeDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

/**
 * @author kk01001
 * date 2022-09-19 22:33:00
 */
@RequiredArgsConstructor
public class S3OssClient implements OssClient {

    private final AmazonS3 amazonS3;

    /**
     * 创建Bucket
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_CreateBucket.html">...</a>
     */
    @Override
    @SneakyThrows
    public void createBucket(String bucketName) {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            amazonS3.createBucket((bucketName));
        }
    }

    /**
     * 获取所有的buckets
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListBuckets.html">...</a>
     */
    @Override
    @SneakyThrows
    public List<Bucket> getAllBuckets() {
        return amazonS3.listBuckets();
    }

    /**
     * 通过Bucket名称删除Bucket
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteBucket.html">...</a>
     */
    @Override
    @SneakyThrows
    public void removeBucket(String bucketName) {
        amazonS3.deleteBucket(bucketName);
    }

    /**
     * 上传对象
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html">...</a>
     */
    @Override
    @SneakyThrows
    public PutObjectResult putObject(String bucketName, String objectName, InputStream stream, String contentType) {
        return putObject(bucketName, objectName, stream, stream.available(), contentType);
    }

    /**
     * 上传对象
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html">...</a>
     */
    @Override
    @SneakyThrows
    public PutObjectResult putObject(String bucketName, String objectName, InputStream stream) {
        return putObject(bucketName, objectName, stream, stream.available(), getDefaultContentType(objectName));
    }

    @Override
    public PutObjectResult putObject(String bucketName, String objectName, File file) {
        return amazonS3.putObject(bucketName, objectName, file);
    }

    @Override
    @SneakyThrows
    public InitiateMultipartUploadResult initiateMultipartUpload(String bucketName, String objectName) {
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, objectName);
        return amazonS3.initiateMultipartUpload(initRequest);
    }


    @Override
    @SneakyThrows
    public UploadPartResult uploadPart(ChunkDTO chunk) {
        try (InputStream in = chunk.getFile().getInputStream()) {
            // 上传
            UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(chunk.getBucketName())
                    .withKey(chunk.getObjectName())
                    .withUploadId(chunk.getUploadId())
                    .withInputStream(in)
                    .withLastPart(chunk.getIsLastPart())
                    .withPartNumber(chunk.getChunkNumber())
                    .withPartSize(chunk.getCurrentChunkSize());
            return amazonS3.uploadPart(uploadRequest);
        }
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(ChunkMergeDTO chunkMerge) {
        CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(chunkMerge.getBucketName(),
                chunkMerge.getObjectName(),
                chunkMerge.getUploadId(),
                chunkMerge.getChunkList());
        return amazonS3.completeMultipartUpload(compRequest);
    }

    /**
     * 通过bucketName和objectName获取对象
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html">...</a>
     */
    @Override
    @SneakyThrows
    public S3Object getObject(String bucketName, String objectName) {
        return amazonS3.getObject(bucketName, objectName);
    }

    /**
     * 获取对象的url
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GeneratePresignedUrl.html">...</a>
     */
    @Override
    @SneakyThrows
    public String getObjectUrl(String bucketName, String objectName, Duration expires) {
        LocalDateTime time = LocalDateTime.now().plusSeconds(expires.toSeconds());
        URL url = amazonS3.generatePresignedUrl(bucketName, objectName, Date.from(time.toInstant(ZoneOffset.UTC)));
        return url.toString();
    }

    /**
     * 通过bucketName和objectName删除对象
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteObject.html">...</a>
     */
    @Override
    @SneakyThrows
    public void removeObject(String bucketName, String objectName) {
        amazonS3.deleteObject(bucketName, objectName);
    }

    /**
     * 根据bucketName和prefix获取对象集合
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListObjects.html">...</a>
     */
    @Override
    @SneakyThrows
    public List<S3ObjectSummary> getAllObjectsByPrefix(String bucketName, String prefix, boolean recursive) {
        ObjectListing objectListing = amazonS3.listObjects(bucketName, prefix);
        return objectListing.getObjectSummaries();
    }

    @Override
    public String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "pdf":
                return "application/pdf";
            case "json":
                return "application/json";
            default:
                return "application/octet-stream";
        }
    }

    @Override
    public AmazonS3 getS3Client() {
        return amazonS3;
    }

    @SneakyThrows
    private PutObjectResult putObject(String bucketName, String objectName, InputStream stream, long size,
                                      String contentType) {

        byte[] bytes = IOUtils.toByteArray(stream);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(size);
        objectMetadata.setContentType(contentType);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        // 上传
        return amazonS3.putObject(bucketName, objectName, byteArrayInputStream, objectMetadata);
    }

    private String getFileExtension(String path) {
        int dotIndex = path.lastIndexOf('.');
        return (dotIndex != -1) ? path.substring(dotIndex + 1).toLowerCase() : "";
    }

    private String getDefaultContentType(String objectName) {
        String fileExtension = getFileExtension(objectName);
        return getContentType(fileExtension);
    }

    /**
     * 下载对象
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html">...</a>
     */
    @Override
    @SneakyThrows
    public InputStream downloadObject(String bucketName, String objectName) {
        S3Object s3Object = amazonS3.getObject(bucketName, objectName);
        return s3Object.getObjectContent();
    }

    /**
     * 下载对象（带进度监听）
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html">...</a>
     */
    @Override
    @SneakyThrows
    public void downloadObject(String bucketName, String objectName, CustomProgressListener progressListener, File destinationFile) {
        TransferManager transferManager = TransferManagerBuilder.standard()
                .withS3Client(amazonS3)
                .build();

        Download download = transferManager.download(bucketName, objectName, destinationFile);
        long totalBytes = download.getObjectMetadata().getContentLength();
        download.addProgressListener(new ProgressListenerAdapter(progressListener, totalBytes));
        // 等待下载完成
        download.waitForCompletion();
    }

    /**
     * 批量删除对象
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteObjects.html">...</a>
     */
    @Override
    @SneakyThrows
    public void removeObjects(String bucketName, List<String> objectNames) {
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
                .withKeys(objectNames.toArray(new String[0]));
        amazonS3.deleteObjects(deleteObjectsRequest);
    }

    /**
     * 获取对象元数据
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObjectMetadata.html">...</a>
     */
    @Override
    @SneakyThrows
    public ObjectMetadata getObjectMetadata(String bucketName, String objectName) {
        return amazonS3.getObjectMetadata(bucketName, objectName);
    }

    /**
     * 更新对象元数据
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_CopyObject.html">...</a>
     */
    @Override
    @SneakyThrows
    public void updateObjectMetadata(String bucketName, String objectName, ObjectMetadata metadata) {
        CopyObjectRequest copyObjRequest = new CopyObjectRequest(bucketName, objectName, bucketName, objectName)
                .withNewObjectMetadata(metadata);
        amazonS3.copyObject(copyObjRequest);
    }

}
