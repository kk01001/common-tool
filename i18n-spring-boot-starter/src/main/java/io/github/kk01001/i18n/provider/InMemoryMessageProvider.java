package io.github.kk01001.i18n.provider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMessageProvider implements I18nMessageProvider {
    
    private final Map<Locale, Map<String, String>> messagesCache = new ConcurrentHashMap<>();
    
    @Override
    public Map<String, String> getMessages(Locale locale) {
        return messagesCache.getOrDefault(locale, new HashMap<>());
    }
    
    /**
     * 添加或更新消息
     */
    public void addMessage(Locale locale, String code, String message) {
        messagesCache.computeIfAbsent(locale, k -> new ConcurrentHashMap<>())
                .put(code, message);
    }
    
    /**
     * 批量添加或更新消息
     */
    public void addMessages(Locale locale, Map<String, String> messages) {
        messagesCache.computeIfAbsent(locale, k -> new ConcurrentHashMap<>())
                .putAll(messages);
    }
    
    /**
     * 删除消息
     */
    public void removeMessage(Locale locale, String code) {
        if (messagesCache.containsKey(locale)) {
            messagesCache.get(locale).remove(code);
        }
    }
    
    @Override
    public void refresh() {
        messagesCache.clear();
    }
} 