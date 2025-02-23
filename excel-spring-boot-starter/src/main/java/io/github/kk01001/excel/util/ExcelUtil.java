package io.github.kk01001.excel.util;

import cn.idev.excel.FastExcel;
import cn.idev.excel.read.listener.PageReadListener;
import cn.idev.excel.write.metadata.WriteSheet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel工具类
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description 提供Excel导出和导入功能
 */
@Slf4j
public class ExcelUtil {

    /**
     * 导出数据到Excel
     *
     * @param data 数据列表
     * @param fileName 导出文件名
     * @param response HTTP响应
     * @param <R> 数据类型
     */
    public static <R> void exportToExcel(List<R> data, String fileName, HttpServletResponse response, Class<R> clazz) throws IOException {
        // 设置响应头
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".xlsx");

        // 写入Excel
        try (var excelWriter = FastExcel.write(response.getOutputStream(), clazz).build()) {
            WriteSheet writeSheet = FastExcel.writerSheet(fileName).build();
            excelWriter.write(data, writeSheet);
            log.info("导出Excel成功: {}", fileName);
        } catch (IOException e) {
            log.error("导出Excel失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 导入Excel数据
     *
     * @param file 上传的Excel文件
     * @param clazz 数据类型
     * @param listener 处理导入数据的监听器
     * @param <R> 数据类型
     */
    public static <R> void importFromExcel(MultipartFile file, Class<R> clazz, PageReadListener<R> listener) throws IOException {
        try {
            FastExcel.read(file.getInputStream(), clazz, listener).sheet().doRead();
            log.info("导入Excel成功: {}", file.getOriginalFilename());
        } catch (IOException e) {
            log.error("导入Excel失败: {}", e.getMessage());
            throw e;
        }
    }
} 