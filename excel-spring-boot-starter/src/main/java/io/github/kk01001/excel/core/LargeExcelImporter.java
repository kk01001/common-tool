package io.github.kk01001.excel.core;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import io.github.kk01001.excel.config.ExcelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class LargeExcelImporter<T> extends AbstractExcelProcessor<T> {

    private final ExcelProperties properties;

    public LargeExcelImporter(ExcelDataHandler<T> dataHandler,
                              Class<T> entityClass,
                              @Qualifier("excelThreadPool") ExecutorService executorService,
                              ExcelProperties properties) {
        super(dataHandler, entityClass, executorService);
        this.properties = properties;
    }

    /**
     * 大数据量Excel导入
     */
    public void importLargeExcel(BigImportContext context) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        BlockingQueue<List<T>> dataQueue = new ArrayBlockingQueue<>(context.getQueueSize());
        AtomicLong totalCount = new AtomicLong(0);
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong failedCount = new AtomicLong(0);
        CountDownLatch processLatch = new CountDownLatch(context.getThreadCount());
        AtomicBoolean readComplete = new AtomicBoolean(false);

        // 使用全局线程池提交处理任务
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < context.getThreadCount(); i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        List<T> batch = dataQueue.poll(100, TimeUnit.MILLISECONDS);
                        if (batch == null) {
                            if (readComplete.get() && dataQueue.isEmpty()) {
                                break;
                            }
                            continue;
                        }
                        try {
                            dataHandler.handleImportData(batch, context);
                            long currentTotal = totalCount.addAndGet(batch.size());
                            successCount.addAndGet(batch.size());

                            if (context.getProgressCallback() != null) {
                                context.getProgressCallback().onProgress(
                                        currentTotal,
                                        -1,
                                        successCount.get(),
                                        failedCount.get()
                                );
                            }

                            log.info("处理一批数据完成, 批次大小: {}, 总处理数: {}, 成功: {}, 失败: {}",
                                    batch.size(), currentTotal, successCount.get(), failedCount.get());
                        } catch (Exception e) {
                            failedCount.addAndGet(batch.size());
                            log.error("处理数据异常", e);
                            if (!context.getContinueOnError()) {
                                throw e;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    processLatch.countDown();
                }
            }, executorService);

            futures.add(future);
        }

        try {
            EasyExcel.read(context.getFile().getInputStream(), entityClass, new ReadListener<T>() {
                private List<T> cachedData = new ArrayList<>(context.getBatchSize());

                @Override
                public void invoke(T data, AnalysisContext analysisContext) {
                    cachedData.add(data);
                    if (cachedData.size() >= context.getBatchSize()) {
                        try {
                            dataQueue.put(new ArrayList<>(cachedData));
                            log.debug("读取一批数据完成, 批次大小: {}", cachedData.size());
                            cachedData.clear();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("数据入队被中断", e);
                        }
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                    if (!cachedData.isEmpty()) {
                        try {
                            dataQueue.put(new ArrayList<>(cachedData));
                            log.info("读取最后一批数据完成, 批次大小: {}", cachedData.size());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("数据入队被中断", e);
                        }
                    }
                    readComplete.set(true);
                }
            }).sheet().doRead();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            futures.forEach(f -> f.cancel(true));
        }

        stopWatch.stop();
        log.info("导入完成, 总处理数据: {}, 耗时: {}秒", totalCount.get(), stopWatch.getTotalTimeSeconds());
    }
} 