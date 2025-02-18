package io.github.kk01001.examples.service;

import io.github.kk01001.examples.dto.UserExcelDTO;
import io.github.kk01001.examples.entity.User;
import io.github.kk01001.examples.mapper.UserMapper;
import io.github.kk01001.excel.core.importer.LargeDataExcelImporter;
import io.github.kk01001.excel.core.importer.LargeDataImportContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class UserDataExcelImporter extends LargeDataExcelImporter<UserExcelDTO> {

    private final UserMapper userMapper;

    public UserDataExcelImporter(@Qualifier("excelThreadPool") ExecutorService executorService, UserMapper userMapper) {
        super(executorService);
        this.userMapper = userMapper;
    }

    @Override
    public void handleImportData(List<UserExcelDTO> dataList, LargeDataImportContext<UserExcelDTO> context) {
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
}