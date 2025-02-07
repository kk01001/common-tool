package io.github.kk01001.excel.core.exporter;

import lombok.Data;

/**
 * 导出上下文
 */
@Data 
public class ExportContext implements Cloneable {

    private String uniqueId;

    private String fileName;

    private String sheetName;

    private Integer pageSize = 1000; // 分页大小

    private Integer currentPage = 1;  // 当前页

    @Override
    public ExportContext clone() {
        try {
            return (ExportContext) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
} 