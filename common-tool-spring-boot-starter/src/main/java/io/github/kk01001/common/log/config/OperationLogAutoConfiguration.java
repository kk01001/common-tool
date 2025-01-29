package io.github.kk01001.common.log.config;

import io.github.kk01001.common.log.annotation.EnableOperationLog;
import io.github.kk01001.common.log.aspect.OperationLogAspect;
import io.github.kk01001.common.log.service.OperationLogHandler;
import io.github.kk01001.common.log.service.OperatorInfoProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.Map;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(annotation = EnableOperationLog.class)
public class OperationLogAutoConfiguration implements ImportAware {

    private AnnotationMetadata importMetadata;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.importMetadata = importMetadata;
    }

    private EnableOperationLog findAnnotation(AnnotationMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableOperationLog.class.getName());
        if (attributes == null) {
            return null;
        }

        Class<? extends OperationLogHandler> handlerClass = (Class<? extends OperationLogHandler>) attributes.get("handler");
        Class<? extends OperatorInfoProvider> providerClass = (Class<? extends OperatorInfoProvider>) attributes.get("provider");

        return new EnableOperationLog() {
            @Override
            public Class<? extends OperationLogHandler> handler() {
                return handlerClass;
            }

            @Override
            public Class<? extends OperatorInfoProvider> provider() {
                return providerClass;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return EnableOperationLog.class;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(OperationLogHandler.class)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public OperationLogHandler operationLogHandler() {
        EnableOperationLog annotation = findAnnotation(importMetadata);
        if (annotation != null && annotation.handler() != OperationLogHandler.class) {
            try {
                return annotation.handler().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("Failed to create custom OperationLogHandler", e);
            }
        }
        return logInfo -> log.info("Operation Log: " + logInfo);
    }

    @Bean
    @ConditionalOnMissingBean(OperatorInfoProvider.class)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public OperatorInfoProvider operatorInfoProvider() {
        EnableOperationLog annotation = findAnnotation(importMetadata);
        if (annotation != null && annotation.provider() != OperatorInfoProvider.class) {
            try {
                return annotation.provider().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("Failed to create custom OperatorInfoProvider", e);
            }
        }
        return OperatorInfoProvider.DEFAULT;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public OperationLogAspect operationLogAspect(OperationLogHandler operationLogHandler,
                                                 OperatorInfoProvider operatorInfoProvider) {
        return new OperationLogAspect(operationLogHandler, operatorInfoProvider);
    }
} 