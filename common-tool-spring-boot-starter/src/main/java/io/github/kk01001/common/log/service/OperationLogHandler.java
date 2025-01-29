package io.github.kk01001.common.log.service;

import io.github.kk01001.common.log.model.OperationLogInfo;

public interface OperationLogHandler {
    /**
     * 处理操作日志
     * @param operationLogInfo 操作日志信息
     */
    void handleLog(OperationLogInfo operationLogInfo);
    
    /**
     * 将通用日志对象转换为自定义日志对象
     * @param operationLogInfo 通用操作日志信息
     * @return 自定义日志对象
     */
    default Object convertLog(OperationLogInfo operationLogInfo) {
        return null;
    }
} 