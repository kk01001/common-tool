package io.github.kk01001.common.i18n.provider;

import java.util.Locale;
import java.util.Map;

public interface I18nMessageProvider {
    
    /**
     * 获取指定语言的所有消息
     *
     * @param locale 语言
     * @return 消息Map key-消息代码 value-消息内容
     */
    Map<String, String> getMessages(Locale locale);
    
    /**
     * 刷新消息
     */
    default void refresh() {
        // 默认空实现
    }
} 