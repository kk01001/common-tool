package io.github.kk01001.examples.excel;

import io.github.kk01001.examples.dto.UserExcelDTO;
import io.github.kk01001.examples.entity.User;
import io.github.kk01001.examples.mapper.UserMapper;
import io.github.kk01001.excel.core.ExcelDataHandler;
import io.github.kk01001.excel.core.ExportContext;
import io.github.kk01001.excel.core.ImportContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserExcelHandler implements ExcelDataHandler<UserExcelDTO> {
    
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleImportData(List<UserExcelDTO> dataList, ImportContext context) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        // 转换为实体
        List<User> users = dataList.stream()
                .map(dto -> {
                    User user = new User();
                    user.setUsername(dto.getUsername());
                    user.setEmail(dto.getEmail());
                    user.setStatus(dto.getStatus());
                    user.setType(dto.getType());
                    return user;
                })
                .toList();

        // log.info("开始批量插入用户数据, 数量: {}", users.size());
        userMapper.insertBatch(users);
        // log.info("批量插入用户数据完成");
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