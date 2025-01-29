package io.github.kk01001.crypto.annotation;

import io.github.kk01001.crypto.config.ParamsCryptoAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ParamsCryptoAutoConfiguration.class)
public @interface EnableParamsCrypto {
} 