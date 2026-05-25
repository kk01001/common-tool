package io.github.archer099.seata.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2025-01-08 15:25:00
 * @description 统一返回结果
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;

    private String msg;

    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> failed() {
        return failed("操作失败");
    }

    public static <T> Result<T> failed(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }
}
