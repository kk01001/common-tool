package io.github.kk01001.excel.core.exporter;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导出上下文
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LargeExcelZipExportContext extends ExportContext implements Cloneable {

    /**
     * 每个sheet最大行数
     */
    private int maxRowsPerSheet = 500000;

    /**
     * 每次从数据库获取的数据量
     */
    private int fetchSize = 5000;

    /**
     * 临时目录,为空则使用系统临时目录
     */
    private String tempDir;

    @Override
    public LargeExcelZipExportContext clone() {
        return (LargeExcelZipExportContext) super.clone();
    }
} 