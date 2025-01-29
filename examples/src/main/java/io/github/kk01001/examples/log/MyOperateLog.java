package io.github.kk01001.examples.log;

import io.github.kk01001.common.log.annotation.OperationType;
import lombok.Data;

/**
 * @author linshiqiang
 * @date 2025-01-29 16:08:00
 * @description
 */
@Data
public class MyOperateLog {
    /**
     * 追踪ID
     */
    private String traceId;

    /**
     * 操作类型
     */
    private OperationType operationType;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 请求体
     */
    private String requestBody;

    /**
     * 响应数据
     */
    private String responseData;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 请求来源（User-Agent）
     */
    private String userAgent;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 操作人部门ID
     */
    private String operatorDeptId;

    /**
     * 操作人部门名称
     */
    private String operatorDeptName;

    /**
     * 结束时间戳(毫秒)
     */
    private Long endTime;

    /**
     * 执行耗时(毫秒)
     */
    private Long duration;

    /**
     * 操作状态（0-成功，1-失败）
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;
}
