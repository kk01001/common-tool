package io.github.kk01001.common.log.annotation;

import io.github.kk01001.common.log.config.OperationLogAutoConfiguration;
import io.github.kk01001.common.log.service.OperationLogHandler;
import io.github.kk01001.common.log.service.OperatorInfoProvider;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用操作日志功能
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(OperationLogAutoConfiguration.class)
public @interface EnableOperationLog {

    /**
     * 操作日志处理器类
     */
    Class<? extends OperationLogHandler> handler() default OperationLogHandler.class;

    /**
     * 操作人信息提供者类
     */
    Class<? extends OperatorInfoProvider> provider() default OperatorInfoProvider.class;
} 