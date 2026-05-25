package io.github.archer099.crypto.annotation;

import io.github.archer099.crypto.config.ParamsCryptoAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ParamsCryptoAutoConfiguration.class)
public @interface EnableParamsCrypto {
} 