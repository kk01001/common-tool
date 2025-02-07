package io.github.kk01001.examples.controller;

import cn.hutool.core.util.IdUtil;
import io.github.kk01001.examples.service.UserDataExcelImporter;
import io.github.kk01001.excel.core.importer.LargeDataImportContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
public class UserImportController {

    private final UserDataExcelImporter userExcelImporter;

    /**
     * 大数据量导入用户
     * @param file Excel文件
     */
    @PostMapping("/users")
    public void importUsers(@RequestParam("file") MultipartFile file) throws Exception {
        LargeDataImportContext context = new LargeDataImportContext();
        context.setUniqueId(IdUtil.fastSimpleUUID());
        context.setFile(file);
        context.setBatchSize(10000);
        context.setQueueSize(10);  // 队列大小设为线程数的2倍
        context.setThreadCount(4);
        context.setEnableTransaction(true);
        context.setContinueOnError(false);

        // 设置进度回调
        context.setProgressCallback((current, total, success, failed) -> 
            log.info("导入进度 - 当前处理: {}, 成功: {}, 失败: {}", current, success, failed)
        );

        userExcelImporter.importLargeExcel(context);
    }

    /**
     * 测试导入 - 支持错误继续
     */
    @PostMapping("/users/continue-on-error")
    public void importUsersWithContinue(@RequestParam("file") MultipartFile file) throws Exception {
        LargeDataImportContext context = new LargeDataImportContext();
        context.setUniqueId(IdUtil.fastSimpleUUID());
        context.setFile(file);
        context.setBatchSize(10000);
        context.setQueueSize(8);
        context.setThreadCount(4);
        context.setEnableTransaction(true);
        context.setContinueOnError(true);  // 错误时继续处理

        context.setProgressCallback((current, total, success, failed) -> 
            log.info("导入进度 - 当前处理: {}, 成功: {}, 失败: {}", current, success, failed)
        );

        log.info("开始导入用户数据(错误继续模式)");
        userExcelImporter.importLargeExcel(context);
    }
}