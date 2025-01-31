package io.github.kk01001.examples.service;

import io.github.kk01001.examples.dto.UserExcelDTO;
import io.github.kk01001.examples.excel.UserExcelHandler;
import io.github.kk01001.excel.config.ExcelProperties;
import io.github.kk01001.excel.core.LargeExcelImporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class UserExcelImporter extends LargeExcelImporter<UserExcelDTO> {

    public UserExcelImporter(UserExcelHandler dataHandler,
                            @Qualifier("excelThreadPool") ExecutorService executorService,
                            ExcelProperties properties) {
        super(dataHandler, UserExcelDTO.class, executorService, properties);
    }
} 