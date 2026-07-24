package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.event.AuditEvent;
import com.shiyu.ai.agent.event.EventPublisher;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务
 * <p>
 * 提供统一的审计记录入口，支持手动记录和通过 AuditEvent 异步消费。
 */
@Slf4j
@Service
public class AuditService {

    private final EventPublisher eventPublisher;

    public AuditService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 记录审计日志（同步）
     */
    public void record(String action, String targetType, String targetId,
                       Object detail, String result, String errorMsg, long durationMs) {
        try {
            Long userId = LoginContextHolder.getUserId();
            Long tenantId = LoginContextHolder.getCurrentTenantId();
            String ip = resolveIp();
            String detailJson = detail != null ? JSONUtils.toJsonString(detail) : null;

            AuditEvent event = new AuditEvent(
                    userId, action, targetType, targetId,
                    detailJson, ip, result, errorMsg, durationMs);

            eventPublisher.publish(event);
        } catch (Exception e) {
            log.warn("记录审计日志失败: action={}", action, e);
        }
    }

    /**
     * 记录成功操作
     */
    public void recordSuccess(String action, String targetType, String targetId, Object detail) {
        record(action, targetType, targetId, detail, "SUCCESS", null, 0);
    }

    /**
     * 记录失败操作
     */
    public void recordFailure(String action, String targetType, String targetId,
                              Object detail, String errorMsg) {
        record(action, targetType, targetId, detail, "FAILED", errorMsg, 0);
    }

    private String resolveIp() {
        try {
            var request = ((org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes());
            if (request != null) {
                return request.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
