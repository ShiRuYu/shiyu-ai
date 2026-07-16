package com.shiyu.ai.agent.config;

import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.agent.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * 审计拦截器
 * <p>
 * 在每次请求完成后自动记录审计日志。
 * 不拦截不需要审计的路径（静态资源、健康检查等）。
 */
@Slf4j
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditService auditService;

    /** 记录请求开始时间 */
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    public AuditInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        START_TIME.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
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

            Map<String, Object> detail = Map.of(
                    "method", method, "path", path, "status", response.getStatus());

            auditService.record(action, targetType, targetId, detail, result, errorMsg, durationMs);

        } catch (Exception e) {
            log.warn("审计拦截器异常: {}", e.getMessage());
        } finally {
            START_TIME.remove();
        }
    }

    private boolean shouldSkip(String path) {
        return path.startsWith("/favicon")
                || path.startsWith("/webjars")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger")
                || path.startsWith("/doc.html")
                || path.startsWith("/actuator")
                || path.startsWith("/uploads");
    }

    private String resolveAction(String method, String path) {
        if (path.startsWith("/api/auth")) return "AUTH_" + method;
        if (path.startsWith("/api/agent")) return "AGENT_" + method;
        if (path.startsWith("/api/knowledge")) return "KNOWLEDGE_" + method;
        if (path.startsWith("/api/education")) return "EDUCATION_" + method;
        if (path.startsWith("/api/record")) return "RECORD_" + method;
        if (path.startsWith("/api/system")) return "SYSTEM_" + method;
        if (path.startsWith("/api/usage")) return "USAGE_" + method;
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
