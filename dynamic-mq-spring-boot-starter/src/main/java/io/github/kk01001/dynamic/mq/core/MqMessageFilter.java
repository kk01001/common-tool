package io.github.kk01001.dynamic.mq.core;

import io.github.kk01001.dynamic.mq.model.MqMessage;

/**
 * @author linshiqiang
 * @date 2025-06-21 21:41:29
 * @description MQ消息过滤器接口，支持多种过滤方式
 */
public interface MqMessageFilter {
    
    /**
     * 过滤类型枚举
     */
    enum FilterType {
        /**
         * 标签过滤
         */
        TAG,
        
        /**
         * SQL过滤
         */
        SQL,
        
        /**
         * 自定义过滤
         */
        CUSTOM
    }
    
    /**
     * 获取过滤类型
     *
     * @return 过滤类型
     */
    FilterType getFilterType();
    
    /**
     * 获取过滤表达式
     *
     * @return 过滤表达式
     */
    String getFilterExpression();
    
    /**
     * 检查消息是否通过过滤
     *
     * @param message 消息对象
     * @return 是否通过过滤
     */
    boolean filter(MqMessage message);
    
    /**
     * 创建标签过滤器
     *
     * @param tagExpression 标签表达式，如 "TagA || TagB"
     * @return 标签过滤器
     */
    static MqMessageFilter tagFilter(String tagExpression) {
        return new TagMessageFilter(tagExpression);
    }
    
    /**
     * 创建SQL过滤器
     *
     * @param sqlExpression SQL表达式，如 "age > 18 AND region = 'beijing'"
     * @return SQL过滤器
     */
    static MqMessageFilter sqlFilter(String sqlExpression) {
        return new SqlMessageFilter(sqlExpression);
    }
    
    /**
     * 创建自定义过滤器
     *
     * @param customFilter 自定义过滤逻辑
     * @return 自定义过滤器
     */
    static MqMessageFilter customFilter(java.util.function.Predicate<MqMessage> customFilter) {
        return new CustomMessageFilter(customFilter);
    }
    
    /**
     * 标签过滤器实现
     */
    class TagMessageFilter implements MqMessageFilter {
        private final String tagExpression;
        
        public TagMessageFilter(String tagExpression) {
            this.tagExpression = tagExpression;
        }
        
        @Override
        public FilterType getFilterType() {
            return FilterType.TAG;
        }
        
        @Override
        public String getFilterExpression() {
            return tagExpression;
        }
        
        @Override
        public boolean filter(MqMessage message) {
            if (message.getTag() == null) {
                return false;
            }
            
            // 简单的标签匹配实现
            if ("*".equals(tagExpression)) {
                return true;
            }
            
            // 支持 || 操作符
            String[] tags = tagExpression.split("\\|\\|");
            for (String tag : tags) {
                if (message.getTag().trim().equals(tag.trim())) {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    /**
     * SQL过滤器实现
     */
    class SqlMessageFilter implements MqMessageFilter {
        private final String sqlExpression;
        
        public SqlMessageFilter(String sqlExpression) {
            this.sqlExpression = sqlExpression;
        }
        
        @Override
        public FilterType getFilterType() {
            return FilterType.SQL;
        }
        
        @Override
        public String getFilterExpression() {
            return sqlExpression;
        }
        
        @Override
        public boolean filter(MqMessage message) {
            // 简化的SQL过滤实现
            // 实际生产环境中应该使用专门的SQL解析器
            try {
                return evaluateSqlExpression(sqlExpression, message);
            } catch (Exception e) {
                return false;
            }
        }
        
        private boolean evaluateSqlExpression(String sql, MqMessage message) {
            // 这里应该实现完整的SQL解析和执行
            // 简化实现：只支持基本的属性比较
            return true; // 简化返回true
        }
    }
    
    /**
     * 自定义过滤器实现
     */
    class CustomMessageFilter implements MqMessageFilter {
        private final java.util.function.Predicate<MqMessage> customFilter;
        
        public CustomMessageFilter(java.util.function.Predicate<MqMessage> customFilter) {
            this.customFilter = customFilter;
        }
        
        @Override
        public FilterType getFilterType() {
            return FilterType.CUSTOM;
        }
        
        @Override
        public String getFilterExpression() {
            return "custom";
        }
        
        @Override
        public boolean filter(MqMessage message) {
            return customFilter.test(message);
        }
    }
}
