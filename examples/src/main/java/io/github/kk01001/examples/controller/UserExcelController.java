package io.github.kk01001.examples.controller;

import cn.hutool.core.util.IdUtil;
import io.github.kk01001.examples.service.UserExcelService;
import io.github.kk01001.excel.core.ExportContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class UserExcelController {

    private final UserExcelService userExcelService;
    private static final Logger log = LoggerFactory.getLogger(UserExcelController.class);

    /**
     * 导出用户数据
     * 支持大数据量导出，会自动分割成多个Excel文件并打包成zip
     */
    @GetMapping("/export/users")
    public void exportUsers(HttpServletResponse response) throws Exception {
        ExportContext context = new ExportContext();
        context.setUniqueId(IdUtil.fastSimpleUUID());
        context.setFileName("用户数据" + System.currentTimeMillis());
        context.setSheetName("用户列表");
        context.setPageSize(50000); // 每页5000条数据

        userExcelService.exportLargeExcelToZip(context, response);
    }
} 