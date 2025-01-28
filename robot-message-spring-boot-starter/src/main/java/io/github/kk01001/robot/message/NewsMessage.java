package io.github.kk01001.robot.message;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新闻消息
 * 用于发送图文新闻类型的消息
 */
@Data
public class NewsMessage implements RobotMessage {
    
    /**
     * 新闻文章列表
     */
    private List<NewsArticle> articles = new ArrayList<>();
    
    /**
     * 新闻文章
     */
    @Data
    public static class NewsArticle {
        /**
         * 文章标题
         */
        private String title;
        
        /**
         * 文章描述
         */
        private String description;
        
        /**
         * 文章链接
         */
        private String url;
        
        /**
         * 图片链接
         */
        private String picUrl;
    }
    
    @Override
    public String getType() {
        return "news";
    }
    
    @Override
    public Map<String, Object> toMessageMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "news");
        
        Map<String, Object> news = new HashMap<>();
        news.put("articles", articles);
        message.put("news", news);
        
        return message;
    }
    
    @Override
    public Map<String, Object> toMessageMap(String robotType) {
        if ("wechat".equals(robotType)) {
            Map<String, Object> message = new HashMap<>();
            message.put("msgtype", "news");
            
            Map<String, Object> news = new HashMap<>();
            List<Map<String, Object>> formattedArticles = new ArrayList<>();
            
            for (NewsArticle article : articles) {
                Map<String, Object> formattedArticle = new HashMap<>();
                formattedArticle.put("title", article.getTitle());
                formattedArticle.put("description", article.getDescription());
                formattedArticle.put("url", article.getUrl());
                formattedArticle.put("picurl", article.getPicUrl());  // 注意：企业微信的字段是picurl而不是picUrl
                formattedArticles.add(formattedArticle);
            }
            
            news.put("articles", formattedArticles);
            message.put("news", news);
            return message;
        }
        
        // 对于其他机器人，返回默认格式
        return toMessageMap();
    }
} 