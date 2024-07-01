package io.github.kk01001.excel.write;

/**
 * @author kk01001
 * date:  2024-06-24 9:21
 */
public abstract class AbstractExcelWriterQueue<T> {

    public abstract ExcelWriterRequest take();

    public abstract void put(ExcelWriterRequest request);


    public void execute() {

    }
}
