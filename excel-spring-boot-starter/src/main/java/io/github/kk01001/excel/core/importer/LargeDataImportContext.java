package io.github.kk01001.excel.core.importer;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 大数据量导入上下文
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LargeDataImportContext extends ImportContext {

    /**
     * 唯一id
     */
    private String uniqueId;

    /**
     * 每批处理的数据量
     */
    private Integer batchSize = 1000;

    /**
     * 缓冲队列大小（可以缓存多少批数据）
     */
    private Integer queueSize = 10;

    /**
     * 处理线程数
     */
    private Integer threadCount = 4;

    /**
     * 是否启用事务
     */
    private Boolean enableTransaction = true;

    /**
     * 失败时是否继续处理
     */
    private Boolean continueOnError = false;

    /**
     * 导入进度回调
     */
    private ImportProgressCallback progressCallback;

    /**
     * 导入进度回调接口
     */
    @FunctionalInterface
    public interface ImportProgressCallback {
        /**
         * 进度更新
         * @param current 当前处理数量
         * @param total 总数量
         * @param success 成功数量
         * @param failed 失败数量
         */
        void onProgress(long current, long total, long success, long failed);
    }
} 