package io.github.kk01001.localmessage.enums;

import lombok.Getter;

/**
 * 本地消息状态枚举
 *
 * @author kk01001
 */
@Getter
public enum MessageStatus {
    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 处理中
     */
    PROCESSING(1, "处理中"),

    /**
     * 处理成功
     */
    SUCCESS(2, "处理成功"),

    /**
     * 处理失败
     */
    FAILED(3, "处理失败"),

    /**
     * 达到最大重试次数，停止重试
     */
    MAX_RETRY_REACHED(4, "达到最大重试次数");

    private final Integer code;
    private final String description;

    MessageStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MessageStatus fromCode(Integer code) {
        for (MessageStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的消息状态码: " + code);
    }
}
