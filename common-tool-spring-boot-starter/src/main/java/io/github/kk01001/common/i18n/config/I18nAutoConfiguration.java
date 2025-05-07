package io.github.kk01001.common.i18n.config;

import io.github.kk01001.common.i18n.properties.I18nProperties;
import io.github.kk01001.common.i18n.provider.I18nManager;
import io.github.kk01001.common.i18n.provider.I18nMessageProvider;
import io.github.kk01001.common.i18n.provider.InMemoryMessageProvider;
import io.github.kk01001.common.i18n.provider.ResourceBundleMessageProvider;
import io.github.kk01001.common.i18n.resolver.CustomLocaleResolver;
import io.github.kk01001.common.i18n.service.I18nService;
import io.github.kk01001.common.i18n.source.CustomMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(I18nProperties.class)
@ConditionalOnProperty(prefix = "i18n", name = "enabled", havingValue = "true")
public class I18nAutoConfiguration {

    private final I18nProperties properties;

    @Bean
    @ConditionalOnMissingBean(I18nMessageProvider.class)
    public I18nMessageProvider messageProvider() {
        if ("memory".equals(properties.getProvider())) {
            return new InMemoryMessageProvider();
        }
        return new ResourceBundleMessageProvider(properties.getBasename());
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageSource messageSource(I18nMessageProvider messageProvider) {
        return new CustomMessageSource(
                messageProvider,
                properties.getAlwaysUseMessageFormat(),
                properties.getUseCodeAsDefaultMessage()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public LocaleResolver localeResolver() {
        return new CustomLocaleResolver(properties.getDefaultLocale());
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nService i18nService(MessageSource messageSource) {
        return new I18nService(messageSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nManager i18nManager(I18nMessageProvider messageProvider) {
        I18nManager manager = new I18nManager(messageProvider);
        manager.refresh();
        return manager;
    }
} 