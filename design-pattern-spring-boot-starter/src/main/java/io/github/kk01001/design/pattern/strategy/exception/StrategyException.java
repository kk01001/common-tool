package io.github.kk01001.design.pattern.strategy.exception;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2025-03-03 09:58:00
 * @description 策略模式相关的异常处理类，包含策略类型和错误码信息
 */
@Getter
public class StrategyException extends RuntimeException {

    private final String strategyType;
    private final String errorCode;

    public StrategyException(String message) {
        super(message);
        this.strategyType = null;
        this.errorCode = null;
    }

    public StrategyException(String message, String strategyType) {
        super(message);
        this.strategyType = strategyType;
        this.errorCode = null;
    }

    public StrategyException(String message, String strategyType, String errorCode) {
        super(message);
        this.strategyType = strategyType;
        this.errorCode = errorCode;
    }

    public StrategyException(String message, String strategyType, String errorCode, Throwable cause) {
        super(message, cause);
        this.strategyType = strategyType;
        this.errorCode = errorCode;
    }
}