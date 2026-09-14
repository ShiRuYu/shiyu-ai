package com.shiyu.ai.common.web.interceptor;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** web拦截器 */
public class WebInvokeInterceptor implements HandlerInterceptor {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    public static final String ACTOR_USER_ID = "shiyu.actor.userId";
    /**
     * 标识，表示当前对象中的对应属性。
     */
    public static final String ACTOR_TENANT_ID = "shiyu.actor.tenantId";

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private static final String TRACE_ID = "traceId";
    private static final String START_NANOS = WebInvokeInterceptor.class.getName() + ".startNanos";
    /**
     * MAX_LOG_VALUE_LENGTH 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final int MAX_LOG_VALUE_LENGTH = 2048;
    private static final String[] SENSITIVE_FIELDS = {
        "token",
        "authorization",
        "password",
        "secret",
        "accessKey",
        "secretKey",
        "apiKey",
        "api-key",
        "api_key",
        "access_token",
        "refresh_token",
        "cookie",
        "prompt",
        "content"
    };
    private static final Pattern SENSITIVE_VALUE_PATTERN =
            Pattern.compile(
                    "(?i)([\\\"']?(?:"
                            + String.join("|", SENSITIVE_FIELDS)
                            + ")[\\\"']?\\s*[:=]\\s*)"
                            + "(\\\"(?:\\\\.|[^\\\"\\\\])*\\\"|'(?:\\\\.|[^'\\\\])*'|[^,}&\\s]+)");

    /**
     * {@code preHandle} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     * @param response 参数值，用于执行当前操作。
     * @param handler 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String traceId = request.getHeader(TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(TRACE_ID, traceId);
        request.setAttribute(START_NANOS, System.nanoTime());

        LoggerUtil.WEB_LOGGER.info(
                "request started method={}, uri={}, traceId={}, userId={}, tenantId={}",
                request.getMethod(),
                request.getRequestURI(),
                traceId,
                request.getAttribute(ACTOR_USER_ID),
                request.getAttribute(ACTOR_TENANT_ID));

        // 打印请求参数
        if (isJsonRequest(request)) {
            String jsonParam = "";
            if (request instanceof RepeatedlyRequestWrapper) {
                BufferedReader reader = request.getReader();
                jsonParam = IoUtil.read(reader);
            }
            LoggerUtil.WEB_LOGGER.debug(
                    "request parameters type=json, parameters={}", sanitize(jsonParam));
        } else {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (MapUtil.isNotEmpty(parameterMap)) {
                LoggerUtil.WEB_LOGGER.debug(
                        "request parameters type=query, parameters={}",
                        sanitize(JSONUtils.toJsonString(parameterMap)));
            }
        }
        return true;
    }

    /**
     * {@code postHandle} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     * @param response 参数值，用于执行当前操作。
     * @param handler 参数值，用于执行当前操作。
     * @param modelAndView 参数值，用于执行当前操作。
     */
    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) {
        // 请求完成日志统一在 afterCompletion 输出。
    }

    /**
     * {@code afterCompletion} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     * @param response 参数值，用于执行当前操作。
     * @param handler 参数值，用于执行当前操作。
     * @param ex 参数值，用于执行当前操作。
     */
    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        try {
            Object startNanos = request.getAttribute(START_NANOS);
            long costMs =
                    startNanos instanceof Long
                            ? (System.nanoTime() - (Long) startNanos) / 1_000_000
                            : -1;
            String message =
                    "request completed method={}, uri={}, status={}, costMs={}, traceId={},"
                            + " userId={}, tenantId={}, error={}";
            Object[] args = {
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                costMs,
                MDC.get(TRACE_ID),
                request.getAttribute(ACTOR_USER_ID),
                request.getAttribute(ACTOR_TENANT_ID),
                ex == null ? null : ex.getMessage()
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
        return contentType != null
                && contentType.regionMatches(
                        true,
                        0,
                        MediaType.APPLICATION_JSON_VALUE,
                        0,
                        MediaType.APPLICATION_JSON_VALUE.length());
    }

    String sanitize(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = SENSITIVE_VALUE_PATTERN.matcher(value);
        StringBuffer result = new StringBuffer(value.length());
        while (matcher.find()) {
            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(matcher.group(1) + maskValue(matcher.group(2))));
        }
        matcher.appendTail(result);
        String sanitized = result.toString();
        return sanitized.length() > MAX_LOG_VALUE_LENGTH
                ? sanitized.substring(0, MAX_LOG_VALUE_LENGTH) + "...(truncated)"
                : sanitized;
    }

    private static String maskValue(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return first + "***" + last;
            }
        }
        return "***";
    }
}
