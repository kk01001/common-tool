package io.github.kk01001.excel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * @author linshiqiang
 * @date 2024-09-11 20:32:00
 * @description
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExporterResultDTO {

    private String uniqueId;

    private String fileDir;

    private Integer excelCount;

    private File firstExcel;
}
