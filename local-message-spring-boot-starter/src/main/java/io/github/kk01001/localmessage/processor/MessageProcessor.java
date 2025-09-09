package io.github.kk01001.localmessage.processor;

import io.github.kk01001.localmessage.entity.LocalMessage;
import lombok.Getter;

/**
 * 消息处理器接口
 * 用户需要实现此接口来处理不同业务类型的消息
 *
 * @author kk01001
 */
public interface MessageProcessor {

    /**
     * 获取支持的业务类型
     *
     * @return 业务类型标识
     */
    String getBusinessType();

    /**
     * 处理消息
     *
     * @param message 本地消息
     * @return 处理结果
     */
    ProcessResult process(LocalMessage message);

    /**
     * 处理结果
     */
    @Getter
    class ProcessResult {
        /**
         * 是否处理成功
         */
        private final boolean success;

        /**
         * 错误信息
         */
        private final String errorMessage;

        /**
         * 是否需要重试
         */
        private final boolean needRetry;

        /**
         * 下次重试延迟时间（秒）
         */
        private final Long retryDelaySeconds;

        private ProcessResult(boolean success, String errorMessage, boolean needRetry, Long retryDelaySeconds) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.needRetry = needRetry;
            this.retryDelaySeconds = retryDelaySeconds;
        }

        /**
         * 创建成功结果
         */
        public static ProcessResult success() {
            return new ProcessResult(true, null, false, null);
        }

        /**
         * 创建失败结果，不重试
         */
        public static ProcessResult failure(String errorMessage) {
            return new ProcessResult(false, errorMessage, false, null);
        }

        /**
         * 创建失败结果，需要重试
         */
        public static ProcessResult failureWithRetry(String errorMessage, Long retryDelaySeconds) {
            return new ProcessResult(false, errorMessage, true, retryDelaySeconds);
        }

        /**
         * 创建失败结果，使用默认重试延迟
         */
        public static ProcessResult failureWithRetry(String errorMessage) {
            return new ProcessResult(false, errorMessage, true, null);
        }

    }
}
