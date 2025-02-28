package io.github.kk01001.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kk01001
 * @date 2025-02-28 14:32:00
 * @description Git信息查询控制器，用于获取由git-commit-id-plugin插件生成的Git仓库详细信息
 */
@RestController
public class GitInfoController {

    /**
     * Git分支名称
     */
    @Value("${git.branch}")
    private String branch;

    /**
     * Git构建时间
     */
    @Value("${git.build.time}")
    private String buildTime;

    /**
     * Git构建版本
     */
    @Value("${git.build.version}")
    private String buildVersion;

    /**
     * Git提交ID（简短版本）
     */
    @Value("${git.commit.id.abbrev}")
    private String commitIdAbbrev;

    /**
     * Git提交ID（完整版本）
     */
    @Value("${git.commit.id.full}")
    private String commitIdFull;

    /**
     * Git提交信息摘要
     */
    @Value("${git.commit.message.short}")
    private String commitMessageShort;

    /**
     * Git提交时间
     */
    @Value("${git.commit.time}")
    private String commitTime;

    /**
     * Git提交用户邮箱
     */
    @Value("${git.commit.user.email}")
    private String commitUserEmail;

    /**
     * Git提交用户名称
     */
    @Value("${git.commit.user.name}")
    private String commitUserName;

    @GetMapping("/git-info")
    public ResponseEntity<Map<String, String>> getGitInfo() {
        Map<String, String> gitInfo = new HashMap<>();
        gitInfo.put("branch", branch);
        gitInfo.put("buildTime", buildTime);
        gitInfo.put("buildVersion", buildVersion);
        gitInfo.put("commitIdAbbrev", commitIdAbbrev);
        gitInfo.put("commitIdFull", commitIdFull);
        gitInfo.put("commitMessageShort", commitMessageShort);
        gitInfo.put("commitTime", commitTime);
        gitInfo.put("commitUserEmail", commitUserEmail);
        gitInfo.put("commitUserName", commitUserName);
        return ResponseEntity.ok(gitInfo);
    }
}
