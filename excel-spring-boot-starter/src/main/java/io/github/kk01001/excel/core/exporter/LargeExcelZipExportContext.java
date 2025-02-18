package io.github.kk01001.excel.core.exporter;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导出上下文
 * @param <R> 返回结果类型
 * @param <P> 查询参数类型
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LargeExcelZipExportContext<R, P> extends ExportContext<R, P> {

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
    public LargeExcelZipExportContext<R, P> clone() {
        return (LargeExcelZipExportContext<R, P>) super.clone();
    }
} 