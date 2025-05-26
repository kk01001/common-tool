package io.github.kk01001.excel.core.exporter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriteConfig;
import cn.hutool.core.text.csv.CsvWriter;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.URLUtil;
import io.github.kk01001.excel.exception.ExportException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public abstract class BigDataCsvlExporter<R, P> {

    protected final Executor excelThreadPool;

    protected BigDataCsvlExporter(@Qualifier("excelThreadPool") Executor excelThreadPool) {
        this.excelThreadPool = excelThreadPool;
    }

    /**
     * 获取要导出的数据
     *
     * @param context 上下文信息
     * @return 数据列表
     */
    public abstract List<R> getExportData(MultiSheetExportContext<R, P> context);

    /**
     * 获取总数据量
     */
    public abstract Long getTotalCount(MultiSheetExportContext<R, P> context);

    public abstract List<String> getHeaderLine(MultiSheetExportContext<R, P> context);

    public abstract List<List<String>> convertToString(List<R> pageDataList);

    // 分隔符
    public abstract char getSeparator();

    /**
     * 导出csv
     *
     * @param context 导出上下文
     */
    public ResponseEntity<StreamingResponseBody> exportCsv(MultiSheetExportContext<R, P> context) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int pageSize = context.getPageSize();

        // 获取总数据量
        long total = getTotalCount(context);
        if (total == 0) {
            log.warn("无数据需要导出");
            throw new ExportException("无数据需要导出");
        }
        File tempFile = FileUtil.file(String.format("/home/temp/%s.csv", IdUtil.fastSimpleUUID()));
        CsvWriteConfig csvWriteConfig = CsvWriteConfig.defaultConfig().setTextDelimiter(getSeparator());
        CsvWriter writer = CsvUtil.getWriter(tempFile, CharsetUtil.CHARSET_UTF_8, true, csvWriteConfig);
        writer.writeHeaderLine(getHeaderLine(context).toArray(new String[0]));
        try {

            // 创建数据队列
            BlockingQueue<List<R>> dataQueue = new LinkedBlockingQueue<>(16);
            AtomicBoolean readComplete = new AtomicBoolean(false);

            // 启动数据读取线程
            List<CompletableFuture<Void>> readFutures = new ArrayList<>();
            int threadCount = Runtime.getRuntime().availableProcessors() - 1;

            int totalPage = PageUtil.totalPage(total, pageSize);
            List<Integer> pageList = IntStream.rangeClosed(1, totalPage)
                    .boxed()
                    .collect(Collectors.toList());

            List<List<Integer>> partition = ListUtil.partition(pageList, threadCount);
            for (List<Integer> pages : partition) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        processData(context, pages, dataQueue);
                    } catch (Exception e) {
                        log.error("处理Csv数据异常", e);
                        throw new CompletionException(e);
                    }
                }, excelThreadPool);
                readFutures.add(future);
            }

            // 启动写入线程
            CompletableFuture<Void> writeFuture = CompletableFuture.runAsync(() -> {
                try {
                    writeFile(dataQueue, readComplete, writer);
                } catch (Exception e) {
                    log.error("写入异常", e);
                    throw new CompletionException(e);
                }
            }, excelThreadPool);

            // 等待所有读取完成
            CompletableFuture.allOf(readFutures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> readComplete.set(true))
                    .join();

            // 等待写入完成
            writeFuture.join();

            writer.flush();
            // 写入文件内容到响应流
            InputStream inputStream = Files.newInputStream(tempFile.toPath());
            StreamingResponseBody responseBody = outputStream -> {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
            };
            stopWatch.stop();
            log.info("Csv导出完成, 耗时: {}秒", stopWatch.getTotalTimeSeconds());
            String fileName = URLUtil.encode(context.getFileName(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentLength(tempFile.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(responseBody);
        } finally {
            writer.close();
            tempFile.delete();
        }
    }

    /**
     * 处理数据
     */
    private void processData(MultiSheetExportContext<R, P> context,
                             List<Integer> pages,
                             BlockingQueue<List<R>> dataQueue) {

        for (Integer page : pages) {
            MultiSheetExportContext<R, P> pageContext = context.clone();
            pageContext.setCurrentPage(page);

            List<R> pageData = getExportData(pageContext);
            if (pageData != null && !pageData.isEmpty()) {
                boolean add = dataQueue.add(pageData);
                log.info("Csv 第{}页数据读取完成, 数据量: {}, 进入队列: {}", page, pageData.size(), add);
            }
        }
    }

    /**
     * 写入文件
     */
    private void writeFile(BlockingQueue<List<R>> dataQueue,
                           AtomicBoolean readComplete,
                           CsvWriter writer) throws Exception {

        while (!readComplete.get() || !dataQueue.isEmpty()) {
            List<R> pageDataList = dataQueue.poll(100, TimeUnit.MILLISECONDS);
            if (CollUtil.isEmpty(pageDataList)) {
                continue;
            }
            writer.write(convertToString(pageDataList));
        }
    }

}