package io.github.kk01001.examples.oss;

import io.github.kk01001.oss.OssProperties;
import io.github.kk01001.oss.client.OssClient;
import io.github.kk01001.oss.listener.ByteProgressListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;

@Slf4j
@RestController
@RequestMapping("/oss/test")
@RequiredArgsConstructor
public class OssTestController {

    private final OssProperties ossProperties;
    private final OssClient ossClient;

    @PostConstruct
    public void createBucket() {
        ossClient.createBucket(ossProperties.getBucketName());
    }

    /**
     * 测试下载文件
     */
    @GetMapping("/download")
    public String downloadFile(@RequestParam String objectName) {
        try (InputStream inputStream = ossClient.downloadObject(ossProperties.getBucketName(), objectName)) {
            // 处理输入流，例如保存到文件
            return "文件下载成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "文件下载失败: " + e.getMessage();
        }
    }

    /**
     * 测试下载文件（带进度条）
     */
    @GetMapping("/download-with-progress")
    public String downloadFileWithProgress(@RequestParam String objectName) {

        File destinationFile = new File("/home/temp/" + objectName);
        try {
            ossClient.downloadObject(ossProperties.getBucketName(), objectName, new ByteProgressListener() {
                @Override
                public void onProgress(long bytesRead, long totalBytes, double percentage) {
                    log.info("下载进度: {}%", percentage);
                }
            }, destinationFile);
            return "文件下载成功（带进度），保存到: " + destinationFile.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "文件下载失败: " + e.getMessage();
        }
    }

    /**
     * 测试上传文件
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam String objectName, @RequestParam MultipartFile file) {
        try {
            ossClient.putObject(ossProperties.getBucketName(), objectName, file.getInputStream(), file.getContentType());
            return "文件上传成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "文件上传失败: " + e.getMessage();
        }
    }
} 