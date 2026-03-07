package io.github.kk01001.chat.example.service;

import java.io.IOException;

/**
 * @author kk01001
 * @date 2026-03-07 19:00:00
 * @description 文件存储策略接口，支持本地存储和 OSS 扩展
 */
public interface FileStorageService {

    /**
     * 存储文件
     *
     * @param data        文件二进制数据
     * @param fileName    原始文件名
     * @param contentType MIME 类型
     * @return 文件访问 URL 或路径
     */
    String store(byte[] data, String fileName, String contentType) throws IOException;

    /**
     * 读取文件
     *
     * @param url 文件 URL 或路径
     * @return 文件二进制数据
     */
    byte[] read(String url) throws IOException;

    /**
     * 删除文件
     *
     * @param url 文件 URL 或路径
     */
    void delete(String url) throws IOException;

    /**
     * 获取存储类型标识
     */
    String getType();
}
