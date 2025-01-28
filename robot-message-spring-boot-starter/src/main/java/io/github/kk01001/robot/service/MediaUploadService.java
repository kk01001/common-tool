package io.github.kk01001.robot.service;

import io.github.kk01001.robot.config.RobotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 媒体文件上传服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaUploadService {

    private final RestTemplate restTemplate;

    private final RobotProperties robotProperties;

    private static final String UPLOAD_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/upload_media";

    /**
     * 上传媒体文件
     *
     * @param robotId     机器人的id
     * @param type        文件类型，可选值：file-普通文件，voice-语音文件
     * @param filename    文件名
     * @param fileContent 文件内容
     * @return 上传结果，包含media_id等信息
     */
    public Map<String, Object> uploadMedia(String robotId, String type, String filename, byte[] fileContent) {
        try {
            RobotProperties.WeChatRobotConfig config = robotProperties.getWeChatRobotConfig(robotId);
            if (config == null) {
                throw new IllegalArgumentException("Invalid robotId: " + robotId);
            }
            String key = config.getKey();
            // 构建请求URL
            String url = String.format("%s?key=%s&type=%s", UPLOAD_URL, key, type);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 构建文件资源
            ByteArrayResource fileResource = new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

            // 构建multipart请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("media", fileResource);

            // 发送请求
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            // 处理响应
            if (response.getBody() != null && "0".equals(String.valueOf(response.getBody().get("errcode")))) {
                log.info("Media uploaded successfully: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("Failed to upload media: {}", response.getBody());
                throw new RuntimeException("Failed to upload media: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Error uploading media", e);
            throw new RuntimeException("Error uploading media", e);
        }
    }
} 