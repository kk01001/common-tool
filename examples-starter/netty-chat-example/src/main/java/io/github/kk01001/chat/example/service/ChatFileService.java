package io.github.archer099.chat.example.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.archer099.chat.example.entity.ChatFile;
import io.github.archer099.chat.example.mapper.ChatFileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author archer099
 * @date 2026-03-07 19:00:00
 * @description 文件服务，使用策略模式存储文件
 */
@Service
@RequiredArgsConstructor
public class ChatFileService extends ServiceImpl<ChatFileMapper, ChatFile> {

    private final FileStorageService fileStorageService;

    public ChatFile upload(MultipartFile file, Long uploaderId) throws IOException {
        byte[] data = file.getBytes();
        String url = fileStorageService.store(data, file.getOriginalFilename(), file.getContentType());

        ChatFile chatFile = new ChatFile();
        chatFile.setFileName(file.getOriginalFilename());
        chatFile.setContentType(file.getContentType());
        chatFile.setFileSize(file.getSize());
        chatFile.setUrl(url);
        chatFile.setStorageType(fileStorageService.getType());
        chatFile.setUploaderId(uploaderId);
        save(chatFile);
        return chatFile;
    }

    /**
     * 从二进制数据直接存储（WebSocket 消息中的文件）
     */
    public ChatFile storeFromBytes(byte[] data, String fileName, String contentType, Long uploaderId) throws IOException {
        String url = fileStorageService.store(data, fileName, contentType);

        ChatFile chatFile = new ChatFile();
        chatFile.setFileName(fileName);
        chatFile.setContentType(contentType);
        chatFile.setFileSize((long) data.length);
        chatFile.setUrl(url);
        chatFile.setStorageType(fileStorageService.getType());
        chatFile.setUploaderId(uploaderId);
        save(chatFile);
        return chatFile;
    }
}
