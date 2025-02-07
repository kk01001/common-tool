package io.github.kk01001.excel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "excel")
public class ExcelProperties {

}