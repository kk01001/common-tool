package io.github.kk01001.excel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2024-09-09 16:15:00
 * @description
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExporterVO {

    private String url;

    private String fileName;
}
