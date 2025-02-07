package io.github.kk01001.excel.core.exporter;

import cn.hutool.core.collection.CollUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.idev.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StopWatch;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class MultiSheetExcelExporter<T> {

    protected final Class<T> entityClass;
    protected final ExecutorService executorService;

    protected MultiSheetExcelExporter(Class<T> entityClass,
                                      @Qualifier("excelThreadPool") ExecutorService executorService) {
        this.entityClass = entityClass;
        this.executorService = executorService;
    }

    /**
     * 获取要导出的数据
     *
     * @param context 上下文信息
     * @return 数据列表
     */
    public abstract List<T> getExportData(ExportContext context);

    /**
     * 获取总数据量
     */
    public abstract Long getTotalCount(ExportContext context);

    /**
     * 导出多个sheet的Excel
     *
     * @param context  导出上下文
     * @param response HTTP响应
     */
    public void exportMultiSheetExcel(MultiSheetExportContext context, HttpServletResponse response) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 设置响应头
        String fileName = URLEncoder.encode(context.getFileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        int pageSize = context.getPageSize();
        int maxRowsPerSheet = context.getMaxRowsPerSheet();

        // 获取总数据量
        long total = getTotalCount(context);
        if (total == 0) {
            log.warn("无数据需要导出");
            return;
        }

        // 计算总页数和sheet分布 key: excel序号, value: 该excel包含的页码列表
        Map<Integer, List<Integer>> sheetPageMap = calculateExcelPageDistribution(total, maxRowsPerSheet, pageSize);

        log.info("开始导出Excel, 文件名: {}, 总数据量: {}, sheet数量: {}",
                fileName, total, sheetPageMap.size());

        // 创建数据队列
        BlockingQueue<Map<Integer, List<T>>> dataQueue = new LinkedBlockingQueue<>(10);
        AtomicBoolean readComplete = new AtomicBoolean(false);

        // 启动数据读取线程
        List<CompletableFuture<Void>> readFutures = new ArrayList<>();
        int threadCount = Math.min(Runtime.getRuntime().availableProcessors(), sheetPageMap.size());

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    processSheetData(context, sheetPageMap, threadIndex, threadCount, dataQueue);
                } catch (Exception e) {
                    log.error("处理sheet数据异常", e);
                    throw new CompletionException(e);
                }
            }, executorService);
            readFutures.add(future);
        }

        // 启动Excel写入线程
        CompletableFuture<Void> writeFuture = CompletableFuture.runAsync(() -> {
            try {
                writeExcelSheets(response, context, dataQueue, readComplete);
            } catch (Exception e) {
                log.error("写入Excel异常", e);
                throw new CompletionException(e);
            }
        }, executorService);

        // 等待所有读取完成
        CompletableFuture.allOf(readFutures.toArray(new CompletableFuture[0]))
                .thenRun(() -> readComplete.set(true))
                .join();

        // 等待写入完成
        writeFuture.join();

        stopWatch.stop();
        log.info("Excel导出完成, 耗时: {}秒", stopWatch.getTotalTimeSeconds());
    }

    /**
     * 计算每个Excel sheet包含的页码分布
     *
     * @param total        总数据量
     * @param maxSheetSize 每个sheet最大行数
     * @param pageSize     每页数据量
     * @return Excel文件页码分布Map, key为Excel序号(从1开始), value为该Excel包含的页码列表
     */
    private Map<Integer, List<Integer>> calculateExcelPageDistribution(long total, int maxSheetSize, int pageSize) {
        Map<Integer, List<Integer>> excelPageMap = new HashMap<>();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int currentExcel = 1;
        int currentRows = 0;

        for (int page = 1; page <= totalPages; page++) {
            if (currentRows >= maxSheetSize) {
                currentExcel++;
                currentRows = 0;
            }
            excelPageMap.computeIfAbsent(currentExcel, k -> new ArrayList<>()).add(page);
            currentRows += pageSize;
        }

        return excelPageMap;
    }

    /**
     * 处理sheet数据
     */
    private void processSheetData(MultiSheetExportContext context,
                                  Map<Integer, List<Integer>> sheetPageMap,
                                  int threadIndex,
                                  int threadCount,
                                  BlockingQueue<Map<Integer, List<T>>> dataQueue) throws InterruptedException {

        for (Map.Entry<Integer, List<Integer>> entry : sheetPageMap.entrySet()) {
            int sheetIndex = entry.getKey();
            // 根据线程索引分配sheet
            if (sheetIndex % threadCount != threadIndex) {
                continue;
            }

            List<Integer> pages = entry.getValue();
            for (Integer page : pages) {
                MultiSheetExportContext pageContext = context.clone();
                pageContext.setCurrentPage(page);

                List<T> pageData = getExportData(pageContext);
                if (pageData != null && !pageData.isEmpty()) {
                    Map<Integer, List<T>> sheetDataMap = new HashMap<>();
                    sheetDataMap.put(sheetIndex, pageData);
                    dataQueue.put(sheetDataMap);
                    log.info("Sheet {} 第{}页数据读取完成, 数据量: {}", sheetIndex, page, pageData.size());
                }
            }
        }
    }

    /**
     * 写入Excel的sheets
     */
    private void writeExcelSheets(HttpServletResponse response,
                                  MultiSheetExportContext context,
                                  BlockingQueue<Map<Integer, List<T>>> dataQueue,
                                  AtomicBoolean readComplete) throws Exception {

        try (ExcelWriter excelWriter = FastExcel.write(response.getOutputStream(), entityClass)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .build()) {

            // 记录每个sheet已写入的数据量
            Map<Integer, Integer> sheetRowCounts = new HashMap<>();
            // 缓存sheet对象
            Map<Integer, WriteSheet> writeSheets = new HashMap<>();

            while (!readComplete.get() || !dataQueue.isEmpty()) {
                Map<Integer, List<T>> pageDataMap = dataQueue.poll(100, TimeUnit.MILLISECONDS);
                if (CollUtil.isEmpty(pageDataMap)) {
                    continue;
                }

                for (Map.Entry<Integer, List<T>> entry : pageDataMap.entrySet()) {
                    int sheetIndex = entry.getKey();
                    List<T> pageData = entry.getValue();

                    if (CollUtil.isNotEmpty(pageData)) {
                        // 获取或创建WriteSheet对象
                        WriteSheet writeSheet = writeSheets.computeIfAbsent(sheetIndex, k -> {
                            String sheetName = getSheetName(context.getSheetName(), k);
                            return FastExcel.writerSheet(k - 1, sheetName).build();
                        });

                        // 写入数据
                        excelWriter.write(pageData, writeSheet);

                        // 更新该sheet的数据量统计
                        int currentCount = sheetRowCounts.getOrDefault(sheetIndex, 0) + pageData.size();
                        sheetRowCounts.put(sheetIndex, currentCount);

                        log.info("Sheet {} 写入一批数据, 批次大小: {}, 当前总量: {}",
                                writeSheet.getSheetName(), pageData.size(), currentCount);
                    }
                }
            }

            // 输出最终统计信息
            sheetRowCounts.forEach((sheetIndex, count) -> {
                WriteSheet writeSheet = writeSheets.get(sheetIndex);
                log.info("Sheet {} 写入完成, 总数据量: {}", writeSheet.getSheetName(), count);
            });
        }
    }

    /**
     * 获取sheet名称
     */
    private String getSheetName(String baseName, int index) {
        return baseName != null && !baseName.isEmpty() ?
                baseName + "_" + index :
                "Sheet" + index;
    }
}