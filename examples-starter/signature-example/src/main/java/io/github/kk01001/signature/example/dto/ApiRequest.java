package io.github.kk01001.signature.example.dto;

import lombok.Data;

/**
 * @author kk01001
 * @date 2026-01-18 13:23:23
 * @description API请求DTO
 */
@Data
public class ApiRequest {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 操作类型
     */
    private String action;

    /**
     * 数据内容
     */
    private String data;
}
