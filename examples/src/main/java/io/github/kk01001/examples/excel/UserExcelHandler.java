package io.github.kk01001.examples.excel;

import io.github.kk01001.examples.dto.UserExcelDTO;
import io.github.kk01001.examples.mapper.UserMapper;
import io.github.kk01001.excel.core.ExcelDataHandler;
import io.github.kk01001.excel.core.ExportContext;
import io.github.kk01001.excel.core.ImportContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserExcelHandler implements ExcelDataHandler<UserExcelDTO> {
    
    private final UserMapper userMapper;

    @Override
    public void handleImportData(List<UserExcelDTO> dataList, ImportContext context) {
        // 处理导入数据
    }

    @Override
    public List<UserExcelDTO> getExportData(ExportContext context) {
        // 分页查询数据
        return userMapper.queryUserExcelList(
                (context.getCurrentPage() - 1) * context.getPageSize(),
                context.getPageSize()
        );
    }
} 