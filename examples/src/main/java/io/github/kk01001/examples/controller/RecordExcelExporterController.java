package io.github.kk01001.examples.controller;

import io.github.kk01001.examples.excel.RecordExcelExporterService;
import io.github.kk01001.examples.excel.RecordQueryDTO;
import io.github.kk01001.excel.model.ExporterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * @date 2024-09-11 20:58:00
 * @description
 */
@RestController
@RequestMapping("excel")
@RequiredArgsConstructor
public class RecordExcelExporterController {

    private final RecordExcelExporterService recordExcelExporterService;

    @PostMapping("exporter")
    public ExporterVO exporter(@RequestBody RecordQueryDTO recordQueryDTO) {

        return recordExcelExporterService.writeExcel(recordQueryDTO);
    }
}
