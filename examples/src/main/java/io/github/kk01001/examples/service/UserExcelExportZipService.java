package io.github.kk01001.examples.service;

import io.github.kk01001.examples.dto.UserExcelDTO;
import io.github.kk01001.examples.mapper.UserMapper;
import io.github.kk01001.excel.core.exporter.LargeExcelZipExportContext;
import io.github.kk01001.excel.core.exporter.LargeExcelZipExporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class UserExcelExportZipService extends LargeExcelZipExporter<UserExcelDTO> {

    private final UserMapper userMapper;

    public UserExcelExportZipService(@Qualifier("excelThreadPool") ExecutorService executorService,
                                     UserMapper userMapper) {
        super(UserExcelDTO.class, executorService);
        this.userMapper = userMapper;
    }

    @Override
    public List<UserExcelDTO> getExportData(LargeExcelZipExportContext context) {
        // 分页查询数据
        return userMapper.queryUserExcelList(
                (context.getCurrentPage() - 1) * context.getPageSize(),
                context.getPageSize()
        );
    }

    @Override
    public Long getTotalCount(LargeExcelZipExportContext context) {
        return userMapper.count();
    }
}