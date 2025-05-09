package io.github.kk01001.i18n.source;

import io.github.kk01001.i18n.provider.I18nMessageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.Nullable;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class CustomMessageSource implements MessageSource {
    
    private final I18nMessageProvider messageProvider;
    private final boolean alwaysUseMessageFormat;
    private final boolean useCodeAsDefaultMessage;
    
    @Override
    public String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
        String msg = resolveMessage(code, locale);
        if (msg == null) {
            msg = useCodeAsDefaultMessage ? code : defaultMessage;
        }
        return formatMessage(msg, args, locale);
    }
    
    @Override
    public String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
        String msg = resolveMessage(code, locale);
        if (msg == null) {
            throw new NoSuchMessageException(code, locale);
        }
        return formatMessage(msg, args, locale);
    }
    
    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();
        if (codes == null) {
            throw new NoSuchMessageException("No codes specified", locale);
        }
        
        for (String code : codes) {
            String msg = resolveMessage(code, locale);
            if (msg != null) {
                return formatMessage(msg, resolvable.getArguments(), locale);
            }
        }
        
        if (resolvable.getDefaultMessage() != null) {
            return formatMessage(resolvable.getDefaultMessage(), resolvable.getArguments(), locale);
        }
        throw new NoSuchMessageException(codes[codes.length - 1], locale);
    }
    
    private String resolveMessage(String code, Locale locale) {
        Map<String, String> messages = messageProvider.getMessages(locale);
        return messages.get(code);
    }
    
    private String formatMessage(String msg, @Nullable Object[] args, Locale locale) {
        if (msg == null || (!alwaysUseMessageFormat && (args == null || args.length == 0))) {
            return msg;
        }
        MessageFormat messageFormat = new MessageFormat(msg, locale);
        return messageFormat.format(args != null ? args : new Object[0]);
    }
} 