package io.github.kk01001.excel.exception;

/**
 * @author linshiqiang
 * @date 2025-05-26 15:17
 * @description
 */
public class ExportException extends RuntimeException {

    private final int code;
    private final String message;

    public ExportException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public ExportException(String message, int code) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
    }

    public ExportException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
