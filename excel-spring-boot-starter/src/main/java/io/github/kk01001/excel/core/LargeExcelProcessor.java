package io.github.kk01001.excel.core;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.idev.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import io.github.kk01001.excel.config.ExcelProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class LargeExcelProcessor<T> extends AbstractExcelProcessor<T> {

    private final ExecutorService executorService;
    private final ExcelProperties properties;
    private final String tempDir;

    public LargeExcelProcessor(ExcelDataHandler<T> dataHandler,
                               Class<T> entityClass,
                               @Qualifier("excelThreadPool") ExecutorService executorService,
                               ExcelProperties properties) {
        super(dataHandler, entityClass, executorService);
        this.executorService = executorService;
        this.properties = properties;
        this.tempDir = StringUtils.hasText(properties.getExport().getTempDir()) ?
                properties.getExport().getTempDir() :
                System.getProperty("java.io.tmpdir") + File.separator + "excel_temp";
        new File(tempDir).mkdirs();
    }

    /**
     * 大数据量导出到ZIP
     */
    public void exportLargeExcelToZip(ExportContext context, HttpServletResponse response) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        long total = getTotalCount(context);
        if (total == 0) {
            return;
        }
        int maxRowsPerFile = properties.getExport().getMaxRowsPerSheet();
        int fetchSize = properties.getExport().getFetchSize();

        String tempExcelDir = tempDir + File.separator + context.getUniqueId();
        File tempExcelDirFile = new File(tempExcelDir);
        tempExcelDirFile.mkdirs();

        // 计算每个Excel文件应该包含哪些页的数据
        Map<Integer, List<Integer>> excelPageMap = calculateExcelPageDistribution(total, maxRowsPerFile, fetchSize);
        log.info("数据总数: {}, maxRowsPerFile: {}, Excel文件分布: {}", total, maxRowsPerFile, excelPageMap);
        // 为每个Excel文件创建一个任务
        List<CompletableFuture<File>> excelTasks = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : excelPageMap.entrySet()) {
            final int excelIndex = entry.getKey();
            final List<Integer> pages = entry.getValue();

            CompletableFuture<File> task = CompletableFuture.supplyAsync(() -> {
                try {
                    // 创建Excel文件
                    String fileName = getFileName(context.getFileName(), excelIndex);
                    File excelFile = new File(tempExcelDir, fileName);
                    return writeExcelFile(excelFile, context, pages, excelIndex, fetchSize);
                } catch (Exception e) {
                    log.error("处理Excel文件{}失败", excelIndex, e);
                    throw new CompletionException(e);
                }
            }, executorService);

            excelTasks.add(task);
        }

        // 等待所有Excel文件生成完成
        List<File> excelFiles = CompletableFuture.allOf(excelTasks.toArray(new CompletableFuture[0]))
                .thenApply(v -> excelTasks.stream()
                        .map(CompletableFuture::join)
                        .toList())
                .join();
        stopWatch.stop();
        log.info("导出Excel完成, 共耗时{}秒, 总数据量: {}, Excel文件数量: {}, 文件路径: {}",
                stopWatch.getTotalTimeSeconds(), total, excelFiles.size(), tempExcelDir);

        // 写入ZIP响应
        response.setContentType("application/zip");
        String encodedFileName = URLEncoder.encode(context.getFileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (File file : excelFiles) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);
                Files.copy(file.toPath(), zos);
                zos.closeEntry();
            }
        } finally {
            // 清理临时文件
            cleanTemp(excelFiles);
            Files.deleteIfExists(tempExcelDirFile.toPath());
        }
    }

    /**
     * 获取总数据量
     */
    protected long getTotalCount(ExportContext context) {
        // 可以由子类实现,用于优化分片
        return 0L;
    }

    private String getSheetName(String baseName, int index) {
        return StringUtils.hasText(baseName) ?
                baseName + "_" + (index + 1) : "Sheet" + (index + 1);
    }

    private String getFileName(String baseName, int index) {
        return baseName + (index == 0 ? "" : "_" + index) + ".xlsx";
    }

    private void cleanTemp(List<File> files) {
        for (File file : files) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (Exception e) {
                log.error("删除临时文件失败: {}", file, e);
            }
        }
    }

    /**
     * 计算每个Excel文件包含的页码分布
     *
     * @param total          总数据量
     * @param maxRowsPerFile 每个文件最大行数
     * @param fetchSize      每页数据量
     * @return Excel文件页码分布Map, key为Excel序号(从1开始), value为该Excel包含的页码列表
     */
    private Map<Integer, List<Integer>> calculateExcelPageDistribution(long total, int maxRowsPerFile, int fetchSize) {
        Map<Integer, List<Integer>> excelPageMap = new HashMap<>();
        int totalPages = (int) Math.ceil((double) total / fetchSize);
        int currentExcel = 1;
        int currentRows = 0;

        for (int page = 1; page <= totalPages; page++) {
            if (currentRows >= maxRowsPerFile) {
                currentExcel++;
                currentRows = 0;
            }
            excelPageMap.computeIfAbsent(currentExcel, k -> new ArrayList<>()).add(page);
            currentRows += fetchSize;
        }

        return excelPageMap;
    }

    /**
     * 写入Excel文件
     */
    private File writeExcelFile(File excelFile,
                                ExportContext context,
                                List<Integer> pages,
                                int excelIndex,
                                int fetchSize) throws IOException {
        try (ExcelWriter excelWriter = FastExcel.write(excelFile, entityClass).build()) {
            String sheetName = getSheetName(context.getSheetName(), excelIndex);
            // 这里注意 如果同一个sheet只要创建一次
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .build();

            // 遍历每一页数据
            for (Integer page : pages) {
                ExportContext pageContext = context.clone();
                pageContext.setCurrentPage(page);
                pageContext.setPageSize(fetchSize);
                List<T> pageData = dataHandler.getExportData(pageContext);
                log.info("处理Excel文件: {}, 读取数据, 第{}页, 数据量: {}", excelFile.getName(), page, pageData.size());
                if (pageData.isEmpty()) {
                    continue;
                }
                excelWriter.write(pageData, writeSheet);
                log.info("处理Excel文件: {}, 写入 数据量: {}", excelFile.getName(), pageData.size());
            }
        }
        return excelFile;
    }
}