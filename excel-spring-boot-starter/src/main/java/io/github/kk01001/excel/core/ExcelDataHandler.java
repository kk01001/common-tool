package io.github.kk01001.excel.core;

import java.util.List;

/**
 * Excel数据处理接口
 */
public interface ExcelDataHandler<T> {
    
    /**
     * 处理导入的数据
     * @param dataList 解析的数据列表
     * @param context 上下文信息
     */
    void handleImportData(List<T> dataList, ImportContext context);

    /**
     * 获取要导出的数据
     * @param context 上下文信息
     * @return 数据列表
     */
    List<T> getExportData(ExportContext context); 
} 