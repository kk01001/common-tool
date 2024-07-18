package io.github.kk01001.util;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.MDC;

/**
 * @author kk01001
 * @since 2022-10-21 16:50
 */
public class TraceIdUtil {

    public static final String TRACE_ID = "requestId";

    /**
     * 构建traceId
     */
    public static String buildTraceId(String reqId, String msgType, String companyCode, String agentId, String extId) {
        String traceIdFormat = "{}@{}@{}@{}@{}";
        return StrFormatter.format(traceIdFormat, reqId,
                msgType,
                companyCode,
                agentId,
                extId);
    }

    /**
     * 构建traceId
     */
    public static String buildTraceId(Object... params) {
        StringBuilder builder = new StringBuilder();
        int length = params.length;
        for (int i = 0; i < length; i++) {
            builder.append(params[i]);
            if (i != length - 1) {
                builder.append(StrUtil.AT);
            }
        }
        return builder.toString();
    }

    public static void buildAndSetTraceId(Object... params) {
        StringBuilder builder = new StringBuilder();
        int length = params.length;
        for (int i = 0; i < length; i++) {
            builder.append(params[i]);
            if (i != length - 1) {
                builder.append(StrUtil.SPACE);
            }
        }
        MDC.put(TRACE_ID, builder.toString());
    }

    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID);
        return traceId == null ? "" : traceId;
    }

    public static void setTraceId(String traceId) {
        if (StrUtil.isNotEmpty(traceId)) {
            MDC.put(TRACE_ID, traceId);
        }
    }

    public static void setTraceId(Object... params) {
        String traceId = buildTraceId(params);
        if (StrUtil.isNotEmpty(traceId)) {
            MDC.put(TRACE_ID, traceId);
        }
    }

    public static void remove() {
        MDC.remove(TRACE_ID);
    }

    public static void clear() {
        MDC.clear();
    }

    public static String generateTraceId() {
        return IdUtil.fastSimpleUUID();
    }
}
