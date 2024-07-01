package io.github.kk01001.excel.write;

import io.github.kk01001.common.ServiceThread;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kk01001
 * date:  2024-06-24 9:14
 */
@Slf4j
public class ExcelWriterService extends ServiceThread {

    @Override
    public String getServiceName() {
        return ExcelWriterService.class.getSimpleName();
    }


    @Override
    public void run() {

    }
}
