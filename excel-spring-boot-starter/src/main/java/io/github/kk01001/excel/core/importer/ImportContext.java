package io.github.kk01001.excel.core.importer;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 导入上下文
 */
@Data
public class ImportContext {

    private MultipartFile file;

    private Integer sheetNo;

    private Integer batchCount = 1000;
}