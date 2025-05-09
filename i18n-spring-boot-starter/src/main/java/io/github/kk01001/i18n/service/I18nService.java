package io.github.kk01001.i18n.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.util.Locale;

@RequiredArgsConstructor
public class I18nService {
    
    private final MessageSource messageSource;
    
    /**
     * 获取国际化消息(使用当前语言)
     *
     * @param code 消息代码
     * @return 国际化消息
     */
    public String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
    
    /**
     * 获取国际化消息(使用当前语言)
     *
     * @param code 消息代码
     * @param args 参数
     * @return 国际化消息
     */
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
    
    /**
     * 获取国际化消息(使用当前语言)
     *
     * @param code 消息代码
     * @param defaultMessage 默认消息
     * @param args 参数
     * @return 国际化消息
     */
    public String getMessage(String code, String defaultMessage, Object... args) {
        return messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }
    
    /**
     * 获取指定语言的国际化消息
     *
     * @param code 消息代码
     * @param locale 语言
     * @param args 参数
     * @return 国际化消息
     */
    public String getMessage(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }
    
    /**
     * 获取指定语言的国际化消息
     *
     * @param code 消息代码
     * @param locale 语言
     * @param defaultMessage 默认消息
     * @param args 参数
     * @return 国际化消息
     */
    public String getMessage(String code, Locale locale, String defaultMessage, Object... args) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }
    
    /**
     * 获取指定语言的国际化消息
     *
     * @param code 消息代码
     * @param locale 语言(如: zh_CN, en_US)
     * @param args 参数
     * @return 国际化消息
     */
    public String getMessageByLocale(String code, String locale, Object... args) {
        return getMessage(code, parseLocale(locale), args);
    }
    
    /**
     * 获取指定语言的国际化消息
     *
     * @param code 消息代码
     * @param locale 语言(如: zh_CN, en_US)
     * @param defaultMessage 默认消息
     * @param args 参数
     * @return 国际化消息
     */
    public String getMessageByLocale(String code, String locale, String defaultMessage, Object... args) {
        return getMessage(code, parseLocale(locale), defaultMessage, args);
    }
    
    /**
     * 解析语言字符串为Locale对象
     */
    private Locale parseLocale(String locale) {
        if (!StringUtils.hasText(locale)) {
            return LocaleContextHolder.getLocale();
        }
        String[] parts = locale.split("_");
        if (parts.length == 2) {
            return Locale.of(parts[0], parts[1]);
        }
        return Locale.of(parts[0]);
    }
} 