# OSS Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kk01001/oss-spring-boot-starter.svg)](https://search.maven.org/artifact/io.github.kk01001/oss-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

## 简介

`oss-spring-boot-starter` 是一个基于 S3 协议的对象存储服务启动器，提供了统一的 API 来操作各种兼容 S3 协议的对象存储服务，如 AWS S3、阿里云 OSS、MinIO 等。通过简单的配置，即可快速集成对象存储功能到 Spring Boot 应用中。

## 功能特点

- 🔌 **开箱即用**：简单的配置即可接入各种对象存储服务
- 🔄 **统一 API**：提供统一的接口操作不同的对象存储服务
- 📁 **完整功能**：支持上传、下载、删除、复制、移动等常用操作
- 📊 **分片上传**：支持大文件分片上传和断点续传
- 🔍 **元数据管理**：支持对象元数据的读取和修改
- 📈 **进度监控**：提供上传/下载进度监控功能
- 🔒 **安全可靠**：支持各种认证方式和安全传输

## 技术栈

- Java 21
- Spring Boot 3.x
- AWS Java SDK for S3

## 快速开始

### 添加依赖

在你的 `pom.xml` 文件中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.kk01001</groupId>
    <artifactId>oss-spring-boot-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

### 配置属性

在 `application.properties` 或 `application.yml` 中配置：

```yaml
oss:
  # 是否启用 OSS 功能
  enable: true
  # 访问密钥ID
  access-key: your-access-key
  # 访问密钥密码
  access-secret: your-access-secret
  # 区域（根据实际服务提供商填写）
  region: your-region
  # 访问端点（服务地址）
  endpoint: https://your-endpoint
  # 存储桶名称
  bucket-name: your-bucket-name
  # 是否启用路径风格访问（true：http://endpoint/bucket，false：http://bucket.endpoint）
  path-style-access: true
  # 最大连接数
  max-connections: 100
```

## 使用示例

### 基本操作

```java
import io.github.kk01001.oss.client.OssClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;

@Service
public class FileService {
    
    @Autowired
    private OssClient ossClient;
    
    // 上传文件
    public void uploadFile(String objectName, File file) throws Exception {
        ossClient.putObject("my-bucket", objectName, file);
    }
    
    // 上传流数据
    public void uploadStream(String objectName, InputStream inputStream, String contentType) throws Exception {
        ossClient.putObject("my-bucket", objectName, inputStream, contentType);
    }
    
    // 下载文件
    public InputStream downloadFile(String objectName) {
        return ossClient.downloadObject("my-bucket", objectName);
    }
    
    // 获取文件访问URL（带有效期）
    public String getFileUrl(String objectName) {
        return ossClient.getObjectUrl("my-bucket", objectName, Duration.ofHours(1));
    }
    
    // 删除文件
    public void deleteFile(String objectName) throws Exception {
        ossClient.removeObject("my-bucket", objectName);
    }
}
```

### 带进度监控的上传和下载

```java
import io.github.kk01001.oss.client.OssClient;
import io.github.kk01001.oss.listener.CustomProgressListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileUploadService {

    @Autowired
    private OssClient ossClient;
    
    public void uploadWithProgress(String objectName, File file) {
        // 创建进度监听器
        CustomProgressListener progressListener = (bytesRead, totalBytes, percentage, speed, speedUnit) -> {
            // 处理进度更新
            System.out.printf("上传进度: %.2f%%, 已上传: %d bytes, 总大小: %d bytes, 速度: %.2f %s%n",
                    percentage, bytesRead, totalBytes, speed, speedUnit);
        };
        
        // 执行带进度监控的上传
        ossClient.putObject("my-bucket", objectName, file, progressListener);
    }
    
    public void downloadWithProgress(String objectName, File destinationFile) {
        // 创建进度监听器
        CustomProgressListener progressListener = (bytesRead, totalBytes, percentage, speed, speedUnit) -> {
            // 处理进度更新
            System.out.printf("下载进度: %.2f%%, 已下载: %d bytes, 总大小: %d bytes, 速度: %.2f %s%n",
                    percentage, bytesRead, totalBytes, speed, speedUnit);
        };
        
        // 执行带进度监控的下载
        ossClient.downloadObject("my-bucket", objectName, progressListener, destinationFile);
    }
}
```

### 分片上传大文件

```java
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartResult;
import io.github.kk01001.oss.client.OssClient;
import io.github.kk01001.oss.model.ChunkDTO;
import io.github.kk01001.oss.model.ChunkMergeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class LargeFileUploadService {

    @Autowired
    private OssClient ossClient;
    
    public String uploadLargeFile(String bucketName, String objectName, List<MultipartFile> chunks) throws Exception {
        // 初始化分片上传任务
        InitiateMultipartUploadResult initResult = ossClient.initiateMultipartUpload(bucketName, objectName);
        String uploadId = initResult.getUploadId();
        
        List<PartETag> partETags = new ArrayList<>();
        int partNumber = 1;
        
        // 上传所有分片
        for (MultipartFile chunk : chunks) {
            ChunkDTO chunkDTO = new ChunkDTO();
            chunkDTO.setUploadId(uploadId);
            chunkDTO.setChunkNumber(partNumber);
            chunkDTO.setBucketName(bucketName);
            chunkDTO.setObjectName(objectName);
            chunkDTO.setFile(chunk);
            
            // 上传分片
            UploadPartResult uploadPartResult = ossClient.uploadPart(chunkDTO);
            partETags.add(uploadPartResult.getPartETag());
            
            partNumber++;
        }
        
        // 完成分片上传
        ChunkMergeDTO mergeDTO = new ChunkMergeDTO();
        mergeDTO.setUploadId(uploadId)
                .setBucketName(bucketName)
                .setObjectName(objectName)
                .setChunkList(partETags);
        
        CompleteMultipartUploadResult result = ossClient.completeMultipartUpload(mergeDTO);
        
        return result.getLocation();
    }
}
```

### 使用 OssUtil 工具类

```java
import io.github.kk01001.oss.OssUtil;
import org.springframework.stereotype.Service;

@Service
public class FilePathService {
    
    // 生成按日期组织的对象名称
    public String generateObjectName(String dir, String fileName) {
        // 生成格式为：dir/yyyy/MM/dd/HH/fileName 的对象名
        return OssUtil.constructObjectName(dir, fileName);
    }
    
    // 生成简单对象名称
    public String generateSimpleObjectName(String dir, String fileName) {
        // 生成格式为：dir/fileName 的对象名
        return OssUtil.constructSimpleObjectName(dir, fileName);
    }
    
    // 构造完整的对象URL
    public String generateObjectUrl(String baseUrl, String dir, String fileName) {
        // 生成格式为：baseUrl/dir/fileName 的URL
        return OssUtil.constructObjectUrl(baseUrl, dir, fileName);
    }
}
```

## 不同存储服务配置示例

### 阿里云 OSS

```yaml
oss:
  access-key: your-aliyun-access-key-id
  access-secret: your-aliyun-access-key-secret
  endpoint: https://oss-cn-hangzhou.aliyuncs.com
  region: cn-hangzhou
  bucket-name: your-bucket-name
  path-style-access: false  # 阿里云OSS使用virtual-hosted-style
```

### AWS S3

```yaml
oss:
  access-key: your-aws-access-key-id
  access-secret: your-aws-secret-access-key
  endpoint: https://s3.amazonaws.com
  region: us-east-1
  bucket-name: your-bucket-name
  path-style-access: false  # AWS S3默认使用virtual-hosted-style
```

### MinIO

```yaml
oss:
  access-key: minioadmin
  access-secret: minioadmin
  endpoint: http://localhost:9000
  region: us-east-1  # MinIO兼容S3，通常使用us-east-1
  bucket-name: your-bucket-name
  path-style-access: true  # MinIO通常使用path-style访问
```

## 高级特性

### 操作对象元数据

```java
import com.amazonaws.services.s3.model.ObjectMetadata;
import io.github.kk01001.oss.client.OssClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetadataService {

    @Autowired
    private OssClient ossClient;
    
    // 获取对象元数据
    public ObjectMetadata getObjectMetadata(String bucketName, String objectName) {
        return ossClient.getObjectMetadata(bucketName, objectName);
    }
    
    // 更新对象元数据
    public void updateObjectMetadata(String bucketName, String objectName, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        
        // 可以设置其他元数据属性
        metadata.addUserMetadata("custom-key", "custom-value");
        
        ossClient.updateObjectMetadata(bucketName, objectName, metadata);
    }
}
```

### 批量操作

```java
import io.github.kk01001.oss.client.OssClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class BatchOperationService {

    @Autowired
    private OssClient ossClient;
    
    // 批量删除对象
    public void batchDeleteObjects(String bucketName, List<String> objectNames) {
        ossClient.removeObjects(bucketName, objectNames);
    }
    
    // 按前缀列出对象
    public void listObjectsByPrefix(String bucketName, String prefix) {
        // 递归查询所有匹配前缀的对象
        ossClient.getAllObjectsByPrefix(bucketName, prefix, true).forEach(item -> {
            System.out.println("对象名称: " + item.getKey());
            System.out.println("对象大小: " + item.getSize() + " 字节");
            System.out.println("上次修改时间: " + item.getLastModified());
        });
    }
}
```

## 常见问题

### Q: 如何创建存储桶（Bucket）?
A: 使用 `ossClient.createBucket("your-bucket-name")` 方法创建新存储桶。

### Q: 如何判断对象是否存在?
A: 可以通过尝试获取对象元数据来判断对象是否存在：
```java
try {
    ossClient.getObjectMetadata(bucketName, objectName);
    return true; // 对象存在
} catch (AmazonS3Exception e) {
    if (e.getStatusCode() == 404) {
        return false; // 对象不存在
    }
    throw e; // 其他错误
}
```

### Q: 如何设置对象的访问权限?
A: 在上传对象时，可以设置对象的访问权限：
```java
ObjectMetadata metadata = new ObjectMetadata();
// 设置其他元数据...

// 创建上传请求并设置权限
PutObjectRequest request = new PutObjectRequest(bucketName, objectName, inputStream, metadata)
    .withCannedAcl(CannedAccessControlList.PublicRead); // 设置为公共读取权限

// 使用原生S3客户端上传
amazonS3.putObject(request);
```

### Q: 如何解决跨域问题?
A: 需要在存储桶上配置CORS规则：
```java
// 获取S3原生客户端
AmazonS3 s3Client = ossClient.getS3Client();

// 创建CORS规则
BucketCrossOriginConfiguration corsConfig = new BucketCrossOriginConfiguration();
List<CORSRule> rules = new ArrayList<>();
CORSRule corsRule = new CORSRule();
corsRule.setAllowedOrigins(List.of("*")); // 允许的来源
corsRule.setAllowedMethods(List.of(CORSRule.AllowedMethods.GET, CORSRule.AllowedMethods.POST));
corsRule.setAllowedHeaders(List.of("*"));
corsRule.setMaxAgeSeconds(3000);
rules.add(corsRule);
corsConfig.setRules(rules);

// 设置CORS规则到存储桶
s3Client.setBucketCrossOriginConfiguration(bucketName, corsConfig);
```

## 最佳实践

1. **使用预签名URL** - 对于临时访问，使用预签名URL而不是直接返回对象内容可以提高性能：
   ```java
   String presignedUrl = ossClient.getObjectUrl("your-bucket", "your-object", Duration.ofMinutes(15));
   ```

2. **合理使用分片上传** - 对于大于5MB的文件，推荐使用分片上传功能，提高上传成功率和性能。

3. **异步处理上传/下载任务** - 对于大文件操作，使用异步方式避免阻塞主线程：
   ```java
   CompletableFuture.runAsync(() -> {
       try {
           ossClient.putObject(bucketName, objectName, file);
       } catch (Exception e) {
           // 处理异常
       }
   });
   ```

4. **合理设置存储桶策略** - 根据实际需求设置合适的存储桶访问策略，避免安全风险。

5. **使用内容类型过滤** - 上传文件时指定正确的ContentType，方便浏览器正确解析：
   ```java
   String contentType = ossClient.getContentType(fileName.substring(fileName.lastIndexOf(".")));
   ossClient.putObject(bucketName, objectName, inputStream, contentType);
   ```

## 许可证

本项目遵循 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源许可证。

## 贡献指南

欢迎提交问题和贡献代码，请通过 GitHub Issue 和 Pull Request 参与项目开发。 