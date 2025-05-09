package io.github.kk01001.i18n.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

public class CustomLocaleResolver implements LocaleResolver {

    private final String defaultLocale;

    public CustomLocaleResolver(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String language = request.getHeader("Accept-Language");
        if (!StringUtils.hasText(language)) {
            // 如果没有语言参数，使用默认语言
            return parseLocale(defaultLocale);
        }
        return parseLocale(language);
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("Cannot change locale - use a different locale resolution strategy");
    }

    private Locale parseLocale(String locale) {
        String[] parts = locale.split("_");
        if (parts.length == 2) {
            return Locale.of(parts[0], parts[1]);
        }
        return Locale.of(parts[0]);
    }
} 