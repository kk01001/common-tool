package io.github.kk01001.excel.core.exporter;

import lombok.Data;

/**
 * 导出上下文
 * @param <R> 返回结果类型
 * @param <P> 查询参数类型
 */
@Data
public class ExportContext<R, P> implements Cloneable {

    private String uniqueId;

    private String fileName;

    private String sheetName;

    private Integer pageSize = 1000; // 分页大小

    private Integer currentPage = 1;  // 当前页

    private P queryParams; // 查询参数

    private Class<R> entityClass; // 实体类型

    @Override
    public ExportContext<R, P> clone() {
        try {
            return (ExportContext<R, P>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
} 