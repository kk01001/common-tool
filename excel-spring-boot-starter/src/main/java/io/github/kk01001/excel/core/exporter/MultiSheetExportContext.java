package io.github.kk01001.excel.core.exporter;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导出上下文
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MultiSheetExportContext extends ExportContext implements Cloneable {

    /**
     * 每个sheet最大行数
     */
    private Integer maxRowsPerSheet = 500000;

    @Override
    public MultiSheetExportContext clone() {
        return (MultiSheetExportContext) super.clone();
    }
} 