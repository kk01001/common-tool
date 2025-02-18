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
public class MultiSheetExportContext<R, P> extends ExportContext<R, P> {

    /**
     * 每个sheet最大行数
     */
    private Integer maxRowsPerSheet = 500000;

    @Override
    public MultiSheetExportContext<R, P> clone() {
        return (MultiSheetExportContext<R, P>) super.clone();
    }
} 