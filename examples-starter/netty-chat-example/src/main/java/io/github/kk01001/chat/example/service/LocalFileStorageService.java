package io.github.archer099.chat.example.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author archer099
 * @date 2026-03-07 19:00:00
 * @description 本地文件存储实现，文件按日期目录存储在项目本地
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "chat.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    @Value("${chat.storage.local.base-path:./upload}")
    private String basePath;

    @PostConstruct
    public void init() throws IOException {
        Path base = Paths.get(basePath);
        if (!Files.exists(base)) {
            Files.createDirectories(base);
        }
        log.info("本地文件存储初始化: basePath={}", base.toAbsolutePath());
    }

    @Override
    public String store(byte[] data, String fileName, String contentType) throws IOException {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String ext = extractExtension(fileName);
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path dir = Paths.get(basePath, dateDir);
        Files.createDirectories(dir);

        Path filePath = dir.resolve(storedName);
        Files.write(filePath, data);

        String relativePath = dateDir + "/" + storedName;
        log.debug("文件存储成功: {} -> {}", fileName, relativePath);
        return relativePath;
    }

    @Override
    public byte[] read(String url) throws IOException {
        Path filePath = Paths.get(basePath, url);
        if (!Files.exists(filePath)) {
            throw new IOException("文件不存在: " + url);
        }
        return Files.readAllBytes(filePath);
    }

    @Override
    public void delete(String url) throws IOException {
        Path filePath = Paths.get(basePath, url);
        Files.deleteIfExists(filePath);
    }

    @Override
    public String getType() {
        return "local";
    }

    private String extractExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(dotIndex) : "";
    }
}
