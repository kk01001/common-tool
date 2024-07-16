package io.github.kk01001.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author kk01001
 * date:  2024-07-02 9:50
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
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
     * @param <T> 泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> ok() {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setTs(System.currentTimeMillis());
        return response;
    }

    /**
     * 构建成功响应
     *
     * @param requestId 请求ID
     * @param <T>       泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> ok(String requestId) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setRequestId(requestId);
        response.setTs(System.currentTimeMillis());
        return response;
    }

    /**
     * 构建成功响应
     *
     * @param message 消息
     * @param <T>     泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> okOfMessage(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setMessage(message);
        response.setTs(System.currentTimeMillis());
        return response;
    }

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
     * @param message   消息
     * @param data      响应数据
     * @param <T>       泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> ok(String requestId, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage(message);
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
     * @param <T> 泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> fail() {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(500);
        response.setMessage("fail");
        response.setTs(System.currentTimeMillis());
        return response;
    }

    /**
     * 构建失败响应
     *
     * @param code 错误代码
     * @param <T>  泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> fail(int code) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage("fail");
        response.setTs(System.currentTimeMillis());
        return response;
    }


    /**
     * 构建失败响应
     *
     * @param message 消息
     * @param code    错误代码
     * @param <T>     泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> failOfMessage(String message, int code) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setTs(System.currentTimeMillis());
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
        return response;
    }

    /**
     * 构建失败响应
     *
     * @param requestId 请求ID
     * @param message   消息
     * @param code      错误代码
     * @param <T>       泛型类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> fail(String requestId, String message, int code) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage("fail");
        response.setMessage(message);
        response.setRequestId(requestId);
        response.setTs(System.currentTimeMillis());
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
        return response;
    }

}
