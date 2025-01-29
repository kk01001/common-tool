package io.github.kk01001.common.log.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.kk01001.common.log.annotation.OperationLog;
import io.github.kk01001.common.log.model.OperationLogInfo;
import io.github.kk01001.common.log.service.OperationLogHandler;
import io.github.kk01001.common.log.service.OperatorInfoProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogHandler operationLogHandler;

    private final OperatorInfoProvider operatorInfoProvider;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        OperationLogInfo logInfo = new OperationLogInfo();
        long startTime = System.currentTimeMillis();
        logInfo.setStartTime(startTime);

        // 记录请求开始时间
        Object result;
        try {
            // 获取请求相关信息
            setRequestInfo(logInfo);
            // 设置操作相关信息
            setOperationInfo(logInfo, point, operationLog);

            // 执行原方法
            result = point.proceed();

            // 设置响应信息
            if (operationLog.saveResponseData() && result != null) {
                logInfo.setResponseData(JSONUtil.toJsonStr(result));
            }
            logInfo.setStatus(0);
        } catch (Exception e) {
            logInfo.setStatus(1);
            logInfo.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            // 设置结束时间和耗时
            long endTime = System.currentTimeMillis();
            logInfo.setEndTime(endTime)
                    .setDuration(endTime - startTime);

            // 处理日志
            operationLogHandler.handleLog(logInfo);
        }

        return result;
    }

    private void setRequestInfo(OperationLogInfo logInfo) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logInfo.setRequestUri(request.getRequestURI())
                    .setRequestMethod(request.getMethod())
                    .setRequestIp(getIpAddress(request))
                    .setUserAgent(request.getHeader("User-Agent"))
                    .setTraceId(MDC.get("traceId"));
        }
    }

    private void setOperationInfo(OperationLogInfo logInfo, ProceedingJoinPoint point, OperationLog operationLog) {
        // 设置操作类型和描述
        logInfo.setOperationType(operationLog.type())
                .setDescription(operationLog.description());

        // 设置请求参数
        if (operationLog.saveRequestData()) {
            Object[] args = point.getArgs();
            logInfo.setRequestParams(JSONUtil.toJsonStr(args));
        }

        // 设置操作人信息
        logInfo.setOperatorId(operatorInfoProvider.getOperatorId())
                .setOperatorName(operatorInfoProvider.getOperatorName())
                .setOperatorDeptId(operatorInfoProvider.getOperatorDeptId())
                .setOperatorDeptName(operatorInfoProvider.getOperatorDeptName());
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 