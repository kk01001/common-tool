package io.github.kk01001.excel.write;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.PageUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import io.github.kk01001.excel.model.ExporterResultDTO;
import io.github.kk01001.excel.model.ExporterVO;
import io.github.kk01001.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @date 2024-09-11 20:09:00
 * @description
 */
@Slf4j
public abstract class AbstractExcelExporter<T, R> {

    public ExporterVO writeExcel(T queryDTO) {
        Long count = getCount(queryDTO);
        if (count == 0) {
            throw new BizException("查询数量为空!");
        }
        Integer maxSheetCount = getMaxSheetCount();
        Integer batchPageSize = getBatchPageSize();
        int totalPage = PageUtil.totalPage(count, batchPageSize);

        long startTotal = System.currentTimeMillis();

        // 分几个excel key excelIndex ; value  table index
        Map<Integer, List<Integer>> excelIndexMap = getExcelIndex(totalPage, maxSheetCount, batchPageSize);

        // 唯一id 临时目录
        String uniqueId = IdUtil.fastUUID();
        String fileDir = getFileDir(uniqueId);
        File firstExcel = null;
        CountDownLatch latch = new CountDownLatch(excelIndexMap.size());
        for (Map.Entry<Integer, List<Integer>> entry : excelIndexMap.entrySet()) {
            Integer excelIndex = entry.getKey();
            List<Integer> tableIndexList = entry.getValue();
            String fileName = getFileFullPath(uniqueId, excelIndex);
            File excelFile = FileUtil.file(fileName);
            if (!excelFile.exists()) {
                try {
                    FileUtil.mkParentDirs(excelFile);
                } catch (Exception e) {
                    log.error("create excel file error: ", e);
                }
            }

            if (Objects.isNull(firstExcel)) {
                firstExcel = excelFile;
            }

            getExecutor().execute(() -> {
                try (ExcelWriter excelWriter = EasyExcel.write(excelFile, getReturnClass()).build()) {
                    WriteSheet writeSheet = EasyExcel.writerSheet(getSheetName(excelIndex))
                            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                            .build();
                    for (Integer index : tableIndexList) {
                        cn.hutool.core.util.PageUtil.setFirstPageNo(1);
                        long startTime = System.currentTimeMillis();
                        int start = PageUtil.getStart(index, batchPageSize);
                        List<R> recordList = getDataPageList(queryDTO, batchPageSize, start);
                        long endTime = System.currentTimeMillis();
                        log.info("查询sql fileName: {}, pageCount: {}, excelIndex: {}, tableIndex: {}, cost: {}ms",
                                fileName, recordList.size(), excelIndex, index, endTime - startTime);

                        excelWriter.write(recordList, writeSheet);
                        long endTime2 = System.currentTimeMillis();
                        log.info("写excel fileName: {}, pageCount: {}, excelIndex: {}, tableIndex: {}, cost: {}ms",
                                fileName, recordList.size(), excelIndex, index, endTime2 - endTime);
                    }
                } catch (Exception e) {
                    log.info("fileName: {}, excelIndex: {}, error: ", fileName, excelIndex, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("latch.await error: ", e);
        }

        long endTotal = System.currentTimeMillis();
        log.info("总数: {}, excel导出总耗时: {}ms", count, endTotal - startTotal);

        ExporterResultDTO exporterResultDTO = ExporterResultDTO.builder()
                .uniqueId(uniqueId)
                .fileDir(fileDir)
                .excelCount(excelIndexMap.size())
                .firstExcel(firstExcel)
                .build();

        try {
            return afterProcess(exporterResultDTO);
        } finally {
            // del tmp file
            FileUtil.del(fileDir);
        }
    }


    /**
     * 线程池
     */
    public abstract Executor getExecutor();

    /**
     * 批量分页查询数量
     */
    public abstract Integer getBatchPageSize();

    /**
     * 每个sheet多大数量
     */
    public abstract Integer getMaxSheetCount();

    /**
     * 查询总数
     *
     * @param queryDTO 查询条件
     * @return 总数
     */
    public abstract Long getCount(T queryDTO);

    /**
     * 分页数据
     *
     * @param queryDTO      查询条件
     * @param batchPageSize 每页数量
     * @param start         开始偏移量
     * @return 分页数据
     */
    public abstract List<R> getDataPageList(T queryDTO, Integer batchPageSize, Integer start);

    /**
     * 数据类型class
     *
     * @return 数据类型class
     */
    public abstract Class<R> getReturnClass();

    /**
     * shell的名称
     *
     * @param excelIndex excel索引 1开始
     * @return shell的名称
     */
    public abstract String getSheetName(Integer excelIndex);

    /**
     * 获取excel临时目录
     *
     * @param uniqueId 当前导出唯一id 作为目录
     * @return excel文件绝对目录  /home/tmp/{uniqueId}
     */
    public abstract String getFileDir(String uniqueId);

    /**
     * excel文件 临时存放目录
     *
     * @param uniqueId   当前导出唯一id 作为目录
     * @param excelIndex excel索引 1开始
     * @return excel文件绝对目录  /home/tmp/{uniqueId}/{excelIndex}.xlsx
     */
    public abstract String getFileFullPath(String uniqueId, Integer excelIndex);

    /**
     * excel写入成功后的操作, excel暂存于磁盘
     */
    public abstract ExporterVO afterProcess(ExporterResultDTO exporterResultDTO);

    /**
     * 每个分页索引 和 Excel的索引对应关系
     *
     * @param totalPage     总的分页数
     * @param maxSheetCount 一个excel 最大数量
     * @param batchPageSize 分页查询 每页数量
     * @return Map
     */
    private Map<Integer, List<Integer>> getExcelIndex(int totalPage, Integer maxSheetCount, Integer batchPageSize) {
        Map<Integer, Integer> indexMap = new HashMap<>();
        Integer totalCount = 0;
        int sheetIndex = 1;
        for (int page = 1; page <= totalPage; page++) {

            if (totalCount < sheetIndex * maxSheetCount) {
                indexMap.put(page, sheetIndex);
                totalCount += batchPageSize;
                continue;
            }
            sheetIndex++;
            totalCount += batchPageSize;
            indexMap.put(page, sheetIndex);
        }
        return indexMap.entrySet()
                .stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,  // 根据value进行分组
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())  // 将key放入列表
                ));
    }
}
