package io.github.kk01001.i18n.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@RequiredArgsConstructor
public class ResourceBundleMessageProvider implements I18nMessageProvider {
    
    private final String basename;
    private final Map<Locale, Map<String, String>> messagesCache = new HashMap<>();
    
    @Override
    public Map<String, String> getMessages(Locale locale) {
        return messagesCache.computeIfAbsent(locale, this::loadMessages);
    }
    
    @Override
    public void refresh() {
        messagesCache.clear();
    }
    
    private Map<String, String> loadMessages(Locale locale) {
        Map<String, String> messages = new HashMap<>();
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String path = basename.replace('.', '/') + "_" + locale.toString() + ".properties";
            Resource resource = resolver.getResource("classpath:" + path);
            
            if (resource.exists()) {
                Properties props = new Properties();
                props.load(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                props.forEach((key, value) -> messages.put(key.toString(), value.toString()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load i18n resource", e);
        }
        return messages;
    }
} 