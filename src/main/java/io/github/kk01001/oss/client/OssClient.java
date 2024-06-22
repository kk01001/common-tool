package io.github.kk01001.oss.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.InputStream;
import java.util.List;

/**
 * @author linshiqiang
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
     * @param contextType 文件类型
     * @throws Exception 异常
     */
    void putObject(String bucketName, String objectName, InputStream stream, String contextType) throws Exception;

    /**
     * 上传文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param stream     文件流
     * @throws Exception 异常
     */
    void putObject(String bucketName, String objectName, InputStream stream) throws Exception;

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
    String getObjectUrl(String bucketName, String objectName, Integer expires);

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
     * @param objectName 文件名称
     * @return ContentType
     */
    String getContentType(String objectName);

    /**
     * 获取AmazonS3
     *
     * @return AmazonS3
     */
    AmazonS3 getS3Client();
}
