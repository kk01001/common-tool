package io.github.kk01001.sensitive.example.controller;

import io.github.kk01001.sensitive.annotation.SensitiveWordCheck;
import io.github.kk01001.sensitive.core.HandleType;
import io.github.kk01001.sensitive.core.MatchType;
import io.github.kk01001.sensitive.core.SensitiveWordResult;
import io.github.kk01001.sensitive.example.dto.ContentCheckRequest;
import io.github.kk01001.sensitive.example.dto.ContentCheckResponse;
import io.github.kk01001.sensitive.example.service.SensitiveWordDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author kk01001
 * @date 2026-01-18 13:02:31
 * @description 敏感词过滤示例控制器
 */
@RestController
@RequestMapping("/api/sensitive")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveWordDemoService demoService;

    /**
     * 检测文本是否包含敏感词
     */
    @PostMapping("/check")
    public ContentCheckResponse check(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return demoService.checkText(text);
    }

    /**
     * 替换敏感词
     */
    @PostMapping("/replace")
    public ContentCheckResponse replace(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String replaceChar = request.getOrDefault("replaceChar", "*");
        return demoService.replaceText(text, replaceChar.charAt(0));
    }

    /**
     * 高亮敏感词
     */
    @PostMapping("/highlight")
    public ContentCheckResponse highlight(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String startTag = request.getOrDefault("startTag", "<span class='sensitive'>");
        String endTag = request.getOrDefault("endTag", "</span>");
        return demoService.highlightText(text, startTag, endTag);
    }

    /**
     * 查找所有敏感词
     */
    @PostMapping("/findAll")
    public List<SensitiveWordResult> findAll(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String matchTypeStr = request.getOrDefault("matchType", "MIN_MATCH");
        MatchType matchType = MatchType.valueOf(matchTypeStr);
        return demoService.findAllSensitiveWords(text, matchType);
    }

    /**
     * 使用注解检测（会抛出异常）
     */
    @PostMapping("/checkWithAnnotation")
    @SensitiveWordCheck(handleType = HandleType.EXCEPTION, message = "内容包含违规词汇")
    public Map<String, Object> checkWithAnnotation(@RequestBody String content) {
        return Map.of(
                "success", true,
                "message", "内容检测通过",
                "content", content
        );
    }

    /**
     * 使用注解自动替换敏感词
     */
    @PostMapping("/replaceWithAnnotation")
    @SensitiveWordCheck(handleType = HandleType.REPLACE, replaceChar = '*')
    public Map<String, Object> replaceWithAnnotation(@RequestBody String content) {
        return Map.of(
                "success", true,
                "message", "内容已处理",
                "content", content
        );
    }

    /**
     * 检测对象中的敏感词（使用注解）
     */
    @PostMapping("/checkObject")
    @SensitiveWordCheck(fields = {"title", "content"}, handleType = HandleType.REPLACE)
    public ContentCheckRequest checkObject(@RequestBody ContentCheckRequest request) {
        return request;
    }

    /**
     * 添加敏感词
     */
    @PostMapping("/addWord")
    public Map<String, Object> addWord(@RequestBody Map<String, String> request) {
        String word = request.get("word");
        String category = request.get("category");
        demoService.addSensitiveWord(word, category);
        return Map.of(
                "success", true,
                "message", "敏感词添加成功",
                "word", word,
                "totalCount", demoService.getSensitiveWordCount()
        );
    }

    /**
     * 批量添加敏感词
     */
    @PostMapping("/addWords")
    public Map<String, Object> addWords(@RequestBody Set<String> words) {
        demoService.addSensitiveWords(words);
        return Map.of(
                "success", true,
                "message", "敏感词批量添加成功",
                "addedCount", words.size(),
                "totalCount", demoService.getSensitiveWordCount()
        );
    }

    /**
     * 移除敏感词
     */
    @PostMapping("/removeWord")
    public Map<String, Object> removeWord(@RequestBody Map<String, String> request) {
        String word = request.get("word");
        demoService.removeSensitiveWord(word);
        return Map.of(
                "success", true,
                "message", "敏感词移除成功",
                "word", word,
                "totalCount", demoService.getSensitiveWordCount()
        );
    }

    /**
     * 获取敏感词数量
     */
    @GetMapping("/count")
    public Map<String, Object> getCount() {
        return Map.of(
                "count", demoService.getSensitiveWordCount()
        );
    }

    /**
     * 添加白名单
     */
    @PostMapping("/addWhiteList")
    public Map<String, Object> addWhiteList(@RequestBody Map<String, String> request) {
        String word = request.get("word");
        demoService.addWhiteList(word);
        return Map.of(
                "success", true,
                "message", "白名单添加成功",
                "word", word
        );
    }
}
