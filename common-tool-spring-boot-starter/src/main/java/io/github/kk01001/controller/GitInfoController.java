package io.github.kk01001.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author kk01001
 * @date 2025-02-28 14:32:00
 * @description Git信息查询控制器，用于获取由git-commit-id-plugin插件生成的Git仓库详细信息
 */
@RestController
@RequiredArgsConstructor
public class GitInfoController {

    private final ResourceLoader resourceLoader;

    private Map<String, String> gitPropertiesMap;

    @PostConstruct
    public void init() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:git.properties");
        if (!resource.exists()) {
            return;
        }
        Properties properties = new Properties();
        properties.load(resource.getInputStream());
        gitPropertiesMap = properties.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue())
                ));
    }

    @GetMapping("/git-info")
    public ResponseEntity<Map<String, String>> getGitInfo() {
        return ResponseEntity.ok(gitPropertiesMap);
    }
}
