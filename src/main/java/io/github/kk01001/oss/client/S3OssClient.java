package io.github.kk01001.oss.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author linshiqiang
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
    public void putObject(String bucketName, String objectName, InputStream stream, String contextType) {
        putObject(bucketName, objectName, stream, stream.available(), contextType);
    }

    /**
     * 上传对象
     * AmazonS3：<a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html">...</a>
     */
    @Override
    @SneakyThrows
    public void putObject(String bucketName, String objectName, InputStream stream) {
        putObject(bucketName, objectName, stream, stream.available(), "application/octet-stream");
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
    public String getObjectUrl(String bucketName, String objectName, Integer expires) {
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, expires);
        URL url = amazonS3.generatePresignedUrl(bucketName, objectName, calendar.getTime());
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

    @SneakyThrows
    private PutObjectResult putObject(String bucketName, String objectName, InputStream stream, long size,
                                      String contextType) {

        byte[] bytes = IOUtils.toByteArray(stream);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(size);
        objectMetadata.setContentType(contextType);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        // 上传
        return amazonS3.putObject(bucketName, objectName, byteArrayInputStream, objectMetadata);
    }

    @Override
    public String getContentType(String objectName) {

        return null;
    }

    @Override
    public AmazonS3 getS3Client() {
        return amazonS3;
    }
}
