package io.github.kk01001.excel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "excel")
public class ExcelProperties {

    /**
     * 线程池配置
     */
    private ThreadPool threadPool = new ThreadPool();

    /**
     * Excel导出配置
     */
    private Export export = new Export();

    @Data
    public static class ThreadPool {
        private int maxParallelism = 100; // 最大并行度
    }

    @Data
    public static class Export {
        
        private int maxRowsPerSheet = 500000; // 每个sheet最大行数

        private int maxSheetsPerFile = 100;   // 每个文件最大sheet数

        private int fetchSize = 5000;         // 每次从数据库获取的数据量

        private String tempDir;               // 临时目录,为空则使用系统临时目录
    }
} 