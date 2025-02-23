package io.github.kk01001.oss;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author kk01001
 * @date 2025-02-13 14:31:00
 * @description OSS工具类
 */
public class OssUtil {

    /**
     * 构造对象名称（带时间戳）
     *
     * @param dir        目录
     * @param fileName   文件名称
     * @return 格式化的对象名称
     */
    public static String constructObjectName(String dir, String fileName) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH");
        String datePath = now.format(formatter);
        return dir + "/" + datePath + "/" + fileName;
    }

    /**
     * 构造对象名称（不带时间戳）
     *
     * @param dir        目录
     * @param fileName   文件名称
     * @return 格式化的对象名称
     */
    public static String constructSimpleObjectName(String dir, String fileName) {
        return dir + "/" + fileName;
    }

    /**
     * 构造完整的对象URL
     *
     * @param baseUrl    OSS的基本URL
     * @param dir        目录
     * @param fileName   文件名称
     * @return 完整的对象URL
     */
    public static String constructObjectUrl(String baseUrl, String dir, String fileName) {
        String objectName = constructSimpleObjectName(dir, fileName);
        return baseUrl + "/" + objectName;
    }
} 