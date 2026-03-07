package io.github.kk01001.chat.example.controller;

import io.github.kk01001.chat.example.config.UserContext;
import io.github.kk01001.chat.example.entity.ChatFile;
import io.github.kk01001.chat.example.service.ChatFileService;
import io.github.kk01001.chat.example.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author kk01001
 * @date 2026-03-07 19:00:00
 * @description 文件上传下载接口
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final ChatFileService chatFileService;
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        ChatFile chatFile = chatFileService.upload(file, UserContext.getUserId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "fileId", chatFile.getId(),
                "fileName", chatFile.getFileName(),
                "contentType", chatFile.getContentType(),
                "fileSize", chatFile.getFileSize(),
                "url", chatFile.getUrl()));
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) throws IOException {
        ChatFile chatFile = chatFileService.getById(fileId);
        if (chatFile == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = fileStorageService.read(chatFile.getUrl());
        String encodedName = URLEncoder.encode(chatFile.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.parseMediaType(chatFile.getContentType()))
                .contentLength(chatFile.getFileSize())
                .body(data);
    }

    @GetMapping("/preview/{fileId}")
    public ResponseEntity<byte[]> preview(@PathVariable Long fileId) throws IOException {
        ChatFile chatFile = chatFileService.getById(fileId);
        if (chatFile == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = fileStorageService.read(chatFile.getUrl());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(chatFile.getContentType()))
                .contentLength(chatFile.getFileSize())
                .body(data);
    }
}
