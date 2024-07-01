package io.github.kk01001.core;

import io.github.kk01001.xxljob.XxlJobConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author kk01001
 * date 2024-06-21 21:00:00
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(XxlJobConfiguration.class)
public @interface EnableXxlJob {
}
