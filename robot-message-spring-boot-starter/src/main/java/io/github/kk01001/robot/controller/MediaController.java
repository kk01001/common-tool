package io.github.kk01001.robot.controller;

import io.github.kk01001.robot.service.MediaUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 媒体文件上传接口
 */
@RestController
@RequestMapping("/robot/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaUploadService mediaUploadService;

    /**
     * 上传媒体文件
     *
     * @param robotId 机器人ID
     * @param type 文件类型（file-普通文件，voice-语音文件）
     * @param file 要上传的文件
     * @return 上传结果，包含media_id等信息
     */
    @PostMapping("/{robotId}/upload")
    public Map<String, Object> uploadMedia(
            @PathVariable("robotId") String robotId,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        // 验证文件类型
        if (!("file".equals(type) || "voice".equals(type))) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        
        // 验证文件大小
        long fileSize = file.getSize();
        if ("file".equals(type) && fileSize > 20 * 1024 * 1024) { // 20MB
            throw new IllegalArgumentException("File size exceeds 20MB limit");
        }
        if ("voice".equals(type) && fileSize > 2 * 1024 * 1024) { // 2MB
            throw new IllegalArgumentException("Voice file size exceeds 2MB limit");
        }
        
        return mediaUploadService.uploadMedia(
            robotId,
            type,
            file.getOriginalFilename(),
            file.getBytes()
        );
    }
} 