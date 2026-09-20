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
 * 校验或约束 Audit 相关的请求、状态和访问规则。
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
     * 执行 Audit 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param auditService 用于完成本次业务处理的 auditService 参数。
     * @param clientIpResolver 用于完成本次业务处理的 clientIpResolver 参数。
     */
    public AuditInterceptor(AuditService auditService, ClientIpResolver clientIpResolver) {
        this.auditService = auditService;
        this.clientIpResolver = clientIpResolver;
    }

    /**
     * 执行 Audit 相关业务数据，并返回处理结果。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @param response 用于完成本次业务处理的 response 参数。
     * @param handler 用于完成本次业务处理的 handler 参数。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        START_TIME.set(System.currentTimeMillis());
        return true;
    }

    /**
     * 执行 Audit 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @param response 用于完成本次业务处理的 response 参数。
     * @param handler 用于完成本次业务处理的 handler 参数。
     * @param ex 用于完成本次业务处理的 ex 参数。
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
