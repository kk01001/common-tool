package com.github.kk0.core;

import com.github.kk0.xxljob.config.XxlJobConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author linshiqiang
 * date 2024-06-21 21:00:00
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(XxlJobConfiguration.class)
public @interface EnableXxlJob {
}
