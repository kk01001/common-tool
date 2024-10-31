package io.github.kk01001.oss.model;

import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2024-10-31 15:53:00
 * @description
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ChunkMergeDTO {

    /**
     * s3 对应的分片上传任务的id
     */
    private String uploadId;

    private String bucketName;

    private String objectName;

    /**
     * 分片信息
     * 每次分片上传成功会返回 partNumber, eTag
     *
     * @see UploadPartResult
     */
    private List<PartETag> chunkList = new ArrayList<>();

}
