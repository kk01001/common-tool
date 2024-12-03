package io.github.kk01001.idempotent.exception;

import lombok.Getter;

/**
 * @author kk01001
 * date 2022/2/22 17:10
 * 异常
 */
@Getter
public class IdempotentException extends RuntimeException {

    private String requestId;

    private String message;

    private Object[] args;

    public IdempotentException() {
    }

    public IdempotentException(String message) {
        this.message = message;
    }

    public IdempotentException(String requestId, String message) {
        this.requestId = requestId;
        this.message = message;
    }

    public IdempotentException(String message, Object... args) {
        this.message = message;
        this.args = args;
    }

    public IdempotentException(String requestId, String message, Object... args) {
        this.requestId = requestId;
        this.message = message;
        this.args = args;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
