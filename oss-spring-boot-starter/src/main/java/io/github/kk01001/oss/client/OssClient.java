package io.github.kk01001.oss.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import io.github.kk01001.oss.listener.ByteProgressListener;
import io.github.kk01001.oss.model.ChunkDTO;
import io.github.kk01001.oss.model.ChunkMergeDTO;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

/**
 * @author kk01001
 * date 2022-09-19 22:32:00
 * Oss 基础操作
 * <a href="https://docs.aws.amazon.com/zh_cn/sdk-for-java/v1/developer-guide/examples-s3-buckets.html">...</a>
 */
public interface OssClient {

    /**
     * 创建bucket
     *
     * @param bucketName bucket名称
     */
    void createBucket(String bucketName);

    /**
     * 获取所有的bucket
     *
     * @return 所有的bucket
     */
    List<Bucket> getAllBuckets();

    /**
     * 通过bucket名称删除bucket
     *
     * @param bucketName 桶名称
     */
    void removeBucket(String bucketName);

    /**
     * 上传文件
     *
     * @param bucketName  bucket名称
     * @param objectName  文件名称
     * @param stream      文件流
     * @param contentType 文件类型
     * @throws Exception 异常
     */
    PutObjectResult putObject(String bucketName, String objectName, InputStream stream, String contentType) throws Exception;

    /**
     * 上传文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param stream     文件流
     * @throws Exception 异常
     */
    PutObjectResult putObject(String bucketName, String objectName, InputStream stream) throws Exception;

    /**
     * 上传文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param file       文件
     * @throws Exception 异常
     */
    PutObjectResult putObject(String bucketName, String objectName, File file) throws Exception;

    /**
     * 创建分配上传任务
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return InitiateMultipartUploadResult
     */
    InitiateMultipartUploadResult initiateMultipartUpload(String bucketName, String objectName);

    /**
     * 分片上传
     *
     * @param chunkDTO 分片信息
     * @return UploadPartResult
     */
    UploadPartResult uploadPart(ChunkDTO chunkDTO);

    /**
     * 完成分片任务
     *
     * @param chunkMergeDTO 分片合并参数
     * @return CompleteMultipartUploadResult
     */
    CompleteMultipartUploadResult completeMultipartUpload(ChunkMergeDTO chunkMergeDTO);

    /**
     * 获取文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return S3Object
     */
    S3Object getObject(String bucketName, String objectName);

    /**
     * 获取对象的url
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param expires    有效期
     * @return 对象的url
     */
    String getObjectUrl(String bucketName, String objectName, Duration expires);

    /**
     * 通过bucketName和objectName删除对象
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @throws Exception 异常
     */
    void removeObject(String bucketName, String objectName) throws Exception;

    /**
     * 根据文件前置查询文件
     *
     * @param bucketName bucket名称
     * @param prefix     前缀
     * @param recursive  是否递归查询
     * @return S3ObjectSummary 列表
     */
    List<S3ObjectSummary> getAllObjectsByPrefix(String bucketName, String prefix, boolean recursive);

    /**
     * 根据文件名称获取 ContentType
     *
     * @param format 文件格式
     * @return ContentType
     */
    String getContentType(String format);

    /**
     * 获取AmazonS3
     *
     * @return AmazonS3
     */
    AmazonS3 getS3Client();

    /**
     * 下载对象
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return InputStream
     */
    InputStream downloadObject(String bucketName, String objectName);

    /**
     * 下载对象（带进度监听）
     *
     * @param bucketName       bucket名称
     * @param objectName       文件名称
     * @param progressListener 进度监听器
     * @param destinationFile  下载保存的目标文件
     */
    void downloadObject(String bucketName, String objectName, ByteProgressListener progressListener, File destinationFile);

    /**
     * 批量删除对象
     *
     * @param bucketName  bucket名称
     * @param objectNames 文件名称列表
     */
    void removeObjects(String bucketName, List<String> objectNames);

    /**
     * 获取对象元数据
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return ObjectMetadata
     */
    ObjectMetadata getObjectMetadata(String bucketName, String objectName);

    /**
     * 更新对象元数据
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param metadata   新的元数据
     */
    void updateObjectMetadata(String bucketName, String objectName, ObjectMetadata metadata);
}
