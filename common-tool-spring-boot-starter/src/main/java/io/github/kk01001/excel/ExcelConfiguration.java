package io.github.kk01001.excel;

import io.github.kk01001.excel.write.ExcelWriterService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * date:  2024-06-24 9:14
 */
@Configuration
@EnableConfigurationProperties(ExcelProperties.class)
public class ExcelConfiguration {

    @Bean
    public ExcelWriterService excelWriterService() {
        return new ExcelWriterService();
    }
}
