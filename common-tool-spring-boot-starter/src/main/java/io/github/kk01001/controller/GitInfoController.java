package io.github.kk01001.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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

    @Value("classpath:git.properties")
    private Resource gitResource;

    private Map<String, String> gitPropertiesMap;

    @PostConstruct
    public void init() throws IOException {
        Properties properties = new Properties();
        properties.load(gitResource.getInputStream());
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
