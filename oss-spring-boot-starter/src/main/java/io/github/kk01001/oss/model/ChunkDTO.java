package io.github.kk01001.oss.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author linshiqiang
 * @date 2024-10-31 15:03:00
 * @description
 */
@Data
public class ChunkDTO {

    /**
     * 分片任务上传id
     */
    private String uploadId;

    /**
     * 当前文件块，从1开始
     */
    private Integer chunkNumber;

    /**
     * 分块大小
     */
    private Long chunkSize;

    /**
     * 当前分块大小
     */
    private Long currentChunkSize;

    /**
     * 总大小
     */
    private Long totalSize;

    /**
     * 桶名称
     */
    private String bucketName;

    /**
     * 文件名
     */
    private String objectName;

    /**
     * 总块数
     */
    private Integer totalChunks;

    /**
     * 是否为最后一块
     * option
     */
    private Boolean isLastPart;

    /**
     * 分块文件内容
     */
    private MultipartFile file;

}
