package io.github.kk01001.examples.service;

import io.github.kk01001.examples.dto.UserExcelDTO;
import io.github.kk01001.examples.excel.UserExcelHandler;
import io.github.kk01001.examples.mapper.UserMapper;
import io.github.kk01001.excel.config.ExcelProperties;
import io.github.kk01001.excel.core.ExportContext;
import io.github.kk01001.excel.core.LargeExcelProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class UserExcelService extends LargeExcelProcessor<UserExcelDTO> {
    
    private final UserMapper userMapper;

    public UserExcelService(UserExcelHandler dataHandler,
                            @Qualifier("excelThreadPool") ExecutorService executorService,
                            ExcelProperties properties, UserMapper userMapper) {
        super(dataHandler, UserExcelDTO.class, executorService, properties);
        this.userMapper = userMapper;
    }

    @Override
    protected long getTotalCount(ExportContext context) {
        return userMapper.count();
    }
} 