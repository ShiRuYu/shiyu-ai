package com.shiyu.ai.web.interceptor;

import com.shiyu.ai.agent.implementation.service.AuditService;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.common.web.auth.ClientIpResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Locale;

/**
 * 审计拦截器
 *
 * <p>在每次请求完成后自动记录审计日志。 不拦截不需要审计的路径（静态资源、健康检查等）。
 */
@Slf4j
public class AuditInterceptor implements HandlerInterceptor {

    /**
     * 审计服务，表示当前对象中的对应属性。
     */
    private final AuditService auditService;
    /**
     * clientIpResolver 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ClientIpResolver clientIpResolver;

    /** 记录请求开始时间 */
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    /**
     * {@code AuditInterceptor} 创建并初始化当前类型实例。
     *
     * @param auditService 参数值，用于执行当前操作。
     * @param clientIpResolver 参数值，用于执行当前操作。
     */
    public AuditInterceptor(AuditService auditService, ClientIpResolver clientIpResolver) {
        this.auditService = auditService;
        this.clientIpResolver = clientIpResolver;
    }

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
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        START_TIME.set(System.currentTimeMillis());
        return true;
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
            Long startTime = START_TIME.get();
            if (startTime == null) return;

            long durationMs = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            String path = request.getRequestURI();

            // 跳过不需要审计的路径
            if (shouldSkip(path)) return;

            String action = resolveAction(method, path);
            String targetType = resolveTargetType(path);
            String targetId = resolveTargetId(path);
            String result = ex == null && response.getStatus() < 400 ? "SUCCESS" : "FAILED";
            String errorMsg = ex != null ? ex.getMessage() : null;

            Map<String, Object> detail =
                    Map.of("method", method, "path", path, "status", response.getStatus());

            var actor = ActorContextHttpAdapter.currentActorOrNull();
            auditService.record(
                    actor == null ? null : actor.tenantId(),
                    actor == null ? null : actor.userId().value(),
                    clientIpResolver.currentClientIp(),
                    action,
                    targetType,
                    targetId,
                    detail,
                    result,
                    errorMsg,
                    durationMs);

        } catch (Exception e) {
            log.warn(
                    "审计拦截器异常: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
        } finally {
            START_TIME.remove();
        }
    }

    private int valueLength(String value) {
        return value == null ? 0 : value.length();
    }

    private boolean shouldSkip(String path) {
        return path.startsWith("/favicon")
                || path.startsWith("/webjars")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/actuator")
                || path.startsWith("/uploads");
    }

    private String resolveAction(String method, String path) {
        if (path.startsWith("/api/iam/auth")) return "AUTH_" + method;
        if (path.startsWith("/api/agent")) return "AGENT_" + method;
        if (path.startsWith("/api/knowledge")) return "KNOWLEDGE_" + method;
        if (path.startsWith("/api/iam")) return "SYSTEM_" + method;
        if (path.startsWith("/api/governance/usage")) return "USAGE_" + method;
        if (path.startsWith("/api/")) {
            String domain = resolveTargetType(path);
            if (!"unknown".equals(domain)) {
                return domain.toUpperCase(Locale.ROOT) + "_" + method;
            }
        }
        return "API_" + method;
    }

    private String resolveTargetType(String path) {
        String[] segments = path.split("/");
        if (segments.length >= 3 && "api".equals(segments[1])) {
            return segments[2];
        }
        return "unknown";
    }

    private String resolveTargetId(String path) {
        String[] segments = path.split("/");
        for (int i = 0; i < segments.length - 1; i++) {
            try {
                Long.parseLong(segments[i]);
                return segments[i];
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
