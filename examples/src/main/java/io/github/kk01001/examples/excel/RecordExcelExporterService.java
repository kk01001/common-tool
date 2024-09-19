package io.github.kk01001.examples.excel;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import io.github.kk01001.excel.model.ExporterResultDTO;
import io.github.kk01001.excel.model.ExporterVO;
import io.github.kk01001.excel.write.AbstractExcelExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author linshiqiang
 * @date 2024-09-11 20:49:00
 * @description
 */
@Slf4j
@Service
public class RecordExcelExporterService extends AbstractExcelExporter<RecordQueryDTO, RecordDTO> {

    public ExporterVO exporter(RecordQueryDTO recordQueryDTO) {
        return writeExcel(recordQueryDTO);
    }

    @Override
    public Executor getExecutor() {
        return ThreadUtil.newExecutor(8);
    }

    @Override
    public Integer getBatchPageSize() {
        return 100000;
    }

    @Override
    public Integer getMaxSheetCount() {
        return 500000;
    }

    @Override
    public Long getCount(RecordQueryDTO queryDTO) {
        return 2000000L;
    }

    @Override
    public List<RecordDTO> getDataPageList(RecordQueryDTO queryDTO, Integer batchPageSize, Integer start) {
        List<RecordDTO> list = new ArrayList<>();

        for (int i = 0; i < batchPageSize; i++) {
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setData1(IdUtil.fastSimpleUUID());
            recordDTO.setData2(IdUtil.fastSimpleUUID());
            recordDTO.setData3(IdUtil.fastSimpleUUID());
            recordDTO.setData4(IdUtil.fastSimpleUUID());
            recordDTO.setData5(IdUtil.fastSimpleUUID());
            recordDTO.setData6(IdUtil.fastSimpleUUID());
            recordDTO.setData7(IdUtil.fastSimpleUUID());
            recordDTO.setData8(IdUtil.fastSimpleUUID());
            recordDTO.setData9(IdUtil.fastSimpleUUID());
            list.add(recordDTO);
        }

        return list;
    }

    @Override
    public Class<RecordDTO> getReturnClass() {
        return RecordDTO.class;
    }

    @Override
    public String getSheetName(Integer excelIndex) {
        return String.format("excelSheet-%s", excelIndex);
    }

    @Override
    public String getFileDir(String uniqueId) {
        return String.format("/home/temp/%s", uniqueId);
    }

    @Override
    public String getFileFullPath(String uniqueId, Integer excelIndex) {
        return String.format("/home/temp/%s/excel-%s.xlsx", uniqueId, excelIndex);
    }

    @Override
    public ExporterVO afterProcess(ExporterResultDTO exporterResultDTO) {
        Integer excelCount = exporterResultDTO.getExcelCount();
        if (excelCount == 1) {
            File firstExcel = exporterResultDTO.getFirstExcel();
            return ExporterVO.builder()
                    .url("http://")
                    .fileName("xxx.xlsx")
                    .build();
        }
        String fileDir = exporterResultDTO.getFileDir();
        // 上传oss
        log.info("afterProcess....");
        ZipUtil.zip(fileDir);
        return ExporterVO.builder()
                .url("http://")
                .fileName("xxx.zip")
                .build();
    }
}
