package io.github.kk01001.robot.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import io.github.kk01001.robot.config.RobotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 媒体文件上传服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaUploadService {

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
            
            // 发送multipart请求
            try (HttpResponse response = HttpRequest.post(url)
                .form("media", fileContent, filename)
                .execute()) {
                
                String body = response.body();
                Map<String, Object> result = JSONUtil.parseObj(body);
                
                // 处理响应
                if ("0".equals(String.valueOf(result.get("errcode")))) {
                    log.info("Media uploaded successfully: {}", result);
                    return result;
                } else {
                    log.error("Failed to upload media: {}", result);
                    throw new RuntimeException("Failed to upload media: " + result);
                }
            }
        } catch (Exception e) {
            log.error("Error uploading media", e);
            throw new RuntimeException("Error uploading media", e);
        }
    }
} 