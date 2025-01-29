package io.github.kk01001.examples.log;

import io.github.kk01001.common.log.model.OperationLogInfo;
import io.github.kk01001.common.log.service.OperationLogHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

@Slf4j
public class CustomOperationLogHandler implements OperationLogHandler {

    @Override
    public void handleLog(OperationLogInfo operationLogInfo) {
        // 转换为自定义日志对象
        MyOperateLog customLog = convertLog(operationLogInfo);
        // 进行处理，比如保存到数据库或发送到消息队列
        log.info("customLog: {}", customLog);
    }

    @Override
    public MyOperateLog convertLog(OperationLogInfo operationLogInfo) {
        MyOperateLog log = new MyOperateLog();
        BeanUtils.copyProperties(operationLogInfo, log);
        return log;
    }
}