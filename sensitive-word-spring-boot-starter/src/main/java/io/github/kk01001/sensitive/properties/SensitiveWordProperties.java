package io.github.kk01001.sensitive.properties;

import io.github.kk01001.sensitive.core.HandleType;
import io.github.kk01001.sensitive.core.MatchType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author kk01001
 * @date 2026-01-18 13:02:31
 * @description 敏感词配置属性
 */
@Data
@ConfigurationProperties(prefix = "sensitive-word")
public class SensitiveWordProperties {
    
    /**
     * 是否启用敏感词过滤
     */
    private boolean enabled = true;
    
    /**
     * 是否忽略大小写
     */
    private boolean ignoreCase = true;
    
    /**
     * 是否跳过空白字符
     */
    private boolean skipWhitespace = true;
    
    /**
     * 需要跳过的特殊字符
     */
    private Set<Character> skipChars = new HashSet<>();
    
    /**
     * 默认匹配类型
     */
    private MatchType matchType = MatchType.MIN_MATCH;
    
    /**
     * 默认处理类型
     */
    private HandleType handleType = HandleType.REPLACE;
    
    /**
     * 替换字符
     */
    private char replaceChar = '*';
    
    /**
     * 替换字符串（优先级高于 replaceChar）
     */
    private String replaceStr;
    
    /**
     * 高亮开始标签
     */
    private String highlightStartTag = "<span class=\"sensitive\">";
    
    /**
     * 高亮结束标签
     */
    private String highlightEndTag = "</span>";
    
    /**
     * 内置敏感词库路径（classpath 下）
     */
    private List<String> dictPaths = new ArrayList<>();
    
    /**
     * 外部敏感词库路径（文件系统）
     */
    private List<String> externalDictPaths = new ArrayList<>();
    
    /**
     * 直接配置的敏感词列表
     */
    private Set<String> words = new HashSet<>();
    
    /**
     * 白名单词汇（不进行过滤）
     */
    private Set<String> whiteList = new HashSet<>();
    
    /**
     * 异常消息模板
     */
    private String exceptionMessage = "内容包含敏感词：{}";
    
    /**
     * Web 过滤器配置
     */
    private Web web = new Web();
    
    /**
     * Web 过滤器配置
     */
    @Data
    public static class Web {
        
        /**
         * 是否启用 Web 过滤器
         */
        private boolean enabled = false;
        
        /**
         * 过滤器拦截路径
         */
        private List<String> urlPatterns = new ArrayList<>();
        
        /**
         * 排除路径
         */
        private List<String> excludePatterns = new ArrayList<>();
        
        /**
         * 需要检测的请求参数名
         */
        private Set<String> checkParams = new HashSet<>();
        
        /**
         * 检测请求体
         */
        private boolean checkBody = true;
        
        /**
         * 发现敏感词时的处理方式
         */
        private HandleType handleType = HandleType.EXCEPTION;
    }
}
