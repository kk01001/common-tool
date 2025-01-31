package io.github.kk01001.excel.core;

import cn.idev.excel.FastExcel;
import cn.idev.excel.read.listener.PageReadListener;
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
public abstract class AbstractExcelProcessor<T> {

    protected final ExcelDataHandler<T> dataHandler;
    protected final Class<T> entityClass;
    protected final ExecutorService executorService;

    protected AbstractExcelProcessor(ExcelDataHandler<T> dataHandler,
                                     Class<T> entityClass,
                                     @Qualifier("excelThreadPool") ExecutorService executorService) {
        this.dataHandler = dataHandler;
        this.entityClass = entityClass;
        this.executorService = executorService;
    }

    /**
     * 导入Excel
     */
    public void importExcel(ImportContext context) throws IOException {
        MultipartFile file = context.getFile();

        // 使用PageReadListener分批处理数据
        FastExcel.read(file.getInputStream(), entityClass, new PageReadListener<T>(dataList -> {
            try {
                dataHandler.handleImportData(dataList, context);
            } catch (Exception e) {
                log.error("处理导入数据异常", e);
                throw new RuntimeException("处理导入数据异常", e);
            }
        }, context.getBatchCount())).sheet().doRead();
    }

    /**
     * 导出Excel
     */
    public void exportExcel(ExportContext context, HttpServletResponse response) throws IOException {
        String fileName = URLEncoder.encode(context.getFileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        List<T> data = dataHandler.getExportData(context);
        FastExcel.write(fileName, entityClass)
                .sheet(fileName)
                .doWrite(data);
    }

}