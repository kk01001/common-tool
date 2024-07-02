package io.github.kk01001.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author kk01001
 * date:  2024-07-02 9:50
 */
@Data
public class ApiResponse<T> {

    /**
     * 响应代码
     */
    @JsonProperty("code")
    private int code;

    private String message;

    /**
     * 请求ID
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 时间戳
     */
    @JsonProperty("ts")
    private Long ts;

    /**
     * 响应数据
     */
    @JsonProperty("data")
    private T data;

    /**
     * 构建成功响应
     *
     * @param requestId 请求ID
     * @param data      响应数据
     * @param <T>       泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> ok(String requestId, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setRequestId(requestId);
        response.setTs(System.currentTimeMillis());
        response.setData(data);
        return response;
    }

    /**
     * 构建成功响应
     *
     * @param requestId 请求ID
     * @param ts        时间戳
     * @param data      响应数据
     * @param <T>       泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> ok(String requestId, Long ts, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setRequestId(requestId);
        response.setTs(ts);
        response.setData(data);
        return response;
    }

    /**
     * 构建成功响应
     *
     * @param requestId 请求ID
     * @param ts        时间戳
     * @param data      响应数据
     * @param message   消息
     * @param <T>       泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> ok(String requestId, Long ts, T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage(message);
        response.setRequestId(requestId);
        response.setTs(ts);
        response.setData(data);
        return response;
    }

    /**
     * 构建失败响应
     *
     * @param requestId 请求ID
     * @param code      错误代码
     * @param <T>       泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> fail(String requestId, int code) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage("fail");
        response.setRequestId(requestId);
        response.setTs(System.currentTimeMillis());
        response.setData(null);
        return response;
    }

    /**
     * 构建失败响应
     *
     * @param requestId 请求ID
     * @param ts        时间戳
     * @param code      错误代码
     * @param <T>       泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> fail(String requestId, Long ts, int code) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage("fail");
        response.setRequestId(requestId);
        response.setTs(ts);
        response.setData(null);
        return response;
    }

    /**
     * 构建失败响应
     *
     * @param requestId 请求ID
     * @param ts        时间戳
     * @param code      错误代码
     * @param message   消息
     * @param <T>       泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> fail(String requestId, Long ts, int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setRequestId(requestId);
        response.setTs(ts);
        response.setData(null);
        return response;
    }

}
