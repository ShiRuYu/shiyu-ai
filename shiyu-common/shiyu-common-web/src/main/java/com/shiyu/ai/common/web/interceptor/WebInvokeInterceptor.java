package com.shiyu.ai.common.web.interceptor;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.domain.UserGlobalContext;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.utils.LoggerUtil;
import com.shiyu.ai.common.web.filter.RepeatedlyRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.util.Map;
import java.util.UUID;

/**
 * web拦截器
 */
public class WebInvokeInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";
    private static final String START_NANOS = WebInvokeInterceptor.class.getName() + ".startNanos";
    private static final int MAX_LOG_VALUE_LENGTH = 2048;
    private static final String[] SENSITIVE_FIELDS = {
            "token", "authorization", "password", "secret", "accessKey", "secretKey",
            "cookie", "prompt", "content"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceId = request.getHeader(TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(TRACE_ID, traceId);
        request.setAttribute(START_NANOS, System.nanoTime());

        LoggerUtil.WEB_LOGGER.info(
                "request started method={}, uri={}, traceId={}, userId={}, tenantId={}",
                request.getMethod(), request.getRequestURI(), traceId,
                LoginContextHolder.getUserId(), LoginContextHolder.getCurrentTenantId());

                // 打印请求参数
        if (isJsonRequest(request)) {
            String jsonParam = "";
            if (request instanceof RepeatedlyRequestWrapper) {
                BufferedReader reader = request.getReader();
                jsonParam = IoUtil.read(reader);
            }
            LoggerUtil.WEB_LOGGER.debug("request parameters type=json, parameters={}", sanitize(jsonParam));
        } else {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (MapUtil.isNotEmpty(parameterMap)) {
                LoggerUtil.WEB_LOGGER.debug("request parameters type=query, parameters={}",
                        sanitize(JSONUtils.toJsonString(parameterMap)));
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                            Object handler, ModelAndView modelAndView) {
        // 请求完成日志统一在 afterCompletion 输出。
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            Object startNanos = request.getAttribute(START_NANOS);
            long costMs = startNanos instanceof Long
                    ? (System.nanoTime() - (Long) startNanos) / 1_000_000
                    : -1;
            String message = "request completed method={}, uri={}, status={}, costMs={}, traceId={}, userId={}, tenantId={}, error={}";
            Object[] args = {
                    request.getMethod(), request.getRequestURI(), response.getStatus(), costMs,
                    MDC.get(TRACE_ID), LoginContextHolder.getUserId(),
                    LoginContextHolder.getCurrentTenantId(), ex == null ? null : ex.getMessage()
            };
            if (ex != null || response.getStatus() >= 500) {
                LoggerUtil.ERROR_LOGGER.error(ex, message, args);
            } else if (response.getStatus() >= 400) {
                LoggerUtil.WEB_LOGGER.warn(message, args);
            } else {
                LoggerUtil.WEB_LOGGER.info(message, args);
            }
        } finally {
            MDC.remove(TRACE_ID);
            UserGlobalContext.clear();
        }
    }

    /**
     * 判断本次请求的数据类型是否为json
     *
     * @param request request
     * @return boolean
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.regionMatches(true, 0,
                MediaType.APPLICATION_JSON_VALUE, 0, MediaType.APPLICATION_JSON_VALUE.length());
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String result = value;
        for (String field : SENSITIVE_FIELDS) {
            result = result.replaceAll("(?i)([\\\"']?" + field
                            + "[\\\"']?\\s*[:=]\\s*[\\\"']?)[^,\\\"'&}\\s]+",
                    "$1***");
        }
        return result.length() > MAX_LOG_VALUE_LENGTH
                ? result.substring(0, MAX_LOG_VALUE_LENGTH) + "...(truncated)"
                : result;
    }
}
