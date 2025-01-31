package io.github.kk01001.excel.core;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 导入上下文
 */
@Data
public class ImportContext {

    private MultipartFile file;

    private String fileName;

    private Integer sheetNo;

    private String sheetName;

    private Integer batchCount = 1000;
}