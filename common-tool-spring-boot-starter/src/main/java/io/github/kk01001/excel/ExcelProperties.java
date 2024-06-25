package io.github.kk01001.excel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author linshiqiang
 * date:  2024-06-24 9:20
 */
@Data
@ConfigurationProperties(prefix = "excel")
public class ExcelProperties {

    private Duration writerQueueTimeout = Duration.ofSeconds(10);
}
