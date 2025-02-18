/*
 * @Author: linshiqiang
 * @Date: 2025-02-06 15:22:29
 * @Description: Do not edit
 */
package io.github.kk01001.excel.core;

import cn.idev.excel.FastExcel;
import cn.idev.excel.read.listener.PageReadListener;
import io.github.kk01001.excel.core.exporter.ExportContext;
import io.github.kk01001.excel.core.importer.ImportContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
public abstract class AbstractSimpleExcelProcessor<R, P> {

    protected final ExecutorService executorService;

    protected AbstractSimpleExcelProcessor(@Qualifier("excelThreadPool") ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * 处理导入的数据
     *
     * @param dataList 解析的数据列表
     * @param context  上下文信息
     */
    public abstract void handleImportData(List<R> dataList, ImportContext context);

    /**
     * 获取要导出的数据
     *
     * @param context 上下文信息
     * @return 数据列表
     */
    public abstract List<R> getExportData(ExportContext<R, P> context);

    /**
     * 导入Excel
     */
    public void importExcel(ImportContext<R> context) throws IOException {
        MultipartFile file = context.getFile();

        // 使用PageReadListener分批处理数据
        FastExcel.read(file.getInputStream(), context.getEntityClass(), new PageReadListener<R>(dataList -> {
            try {
                handleImportData(dataList, context);
            } catch (Exception e) {
                log.error("处理导入数据异常", e);
                throw new RuntimeException("处理导入数据异常", e);
            }
        }, context.getBatchCount())).sheet().doRead();
    }

    /**
     * 导出Excel
     */
    public void exportExcel(ExportContext<R, P> context, HttpServletResponse response) throws IOException {
        String fileName = URLEncoder.encode(context.getFileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        List<R> data = getExportData(context);
        FastExcel.write(fileName, context.getEntityClass())
                .sheet(fileName)
                .doWrite(data);
    }

}