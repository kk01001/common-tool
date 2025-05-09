package io.github.kk01001.i18n.provider;

import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class I18nManager {

    private final I18nMessageProvider messageProvider;

    public void addMessage(String locale, String code, String message) {
        if (messageProvider instanceof InMemoryMessageProvider memoryProvider) {
            memoryProvider.addMessage(Locale.forLanguageTag(locale.replace('_', '-')), code, message);
        }
    }

    public void addMessages(String locale, Map<String, String> messages) {
        if (messageProvider instanceof InMemoryMessageProvider memoryProvider) {
            memoryProvider.addMessages(Locale.forLanguageTag(locale.replace('_', '-')), messages);
        }
    }

    public void removeMessage(String locale, String code) {
        if (messageProvider instanceof InMemoryMessageProvider memoryProvider) {
            memoryProvider.removeMessage(Locale.forLanguageTag(locale.replace('_', '-')), code);
        }
    }

    public void refresh() {
        messageProvider.refresh();
    }
}