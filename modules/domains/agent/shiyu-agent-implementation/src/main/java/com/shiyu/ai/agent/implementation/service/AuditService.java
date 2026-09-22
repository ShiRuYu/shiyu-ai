package com.shiyu.ai.agent.implementation.service;

import com.shiyu.ai.agent.implementation.event.model.AuditEvent;
import com.shiyu.ai.agent.implementation.event.publisher.EventPublisher;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.kernel.context.TenantId;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * 提供 Audit 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class AuditService {

    /**
     * eventPublisher 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EventPublisher eventPublisher;

    /**
     * 执行 Audit 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param eventPublisher 用于完成本次业务处理的 eventPublisher 参数。
     */
    public AuditService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /** 记录审计日志（同步） */
    public void record(
            TenantId tenantId,
            Long userId,
            String ip,
            String action,
            String targetType,
            String targetId,
            Object detail,
            String result,
            String errorMsg,
            long durationMs) {
        try {
            String detailJson = detail != null ? JSONUtils.toJsonString(detail) : null;

            AuditEvent event =
                    new AuditEvent(
                            tenantId,
                            userId,
                            action,
                            targetType,
                            targetId,
                            detailJson,
                            ip,
                            result,
                            errorMsg,
                            durationMs);

            eventPublisher.publish(event);
        } catch (Exception e) {
            log.warn("记录审计日志失败: action={}", action, e);
        }
    }

    /** 记录成功操作 */
    public void recordSuccess(
            TenantId tenantId,
            Long userId,
            String ip,
            String action,
            String targetType,
            String targetId,
            Object detail) {
        record(tenantId, userId, ip, action, targetType, targetId, detail, "SUCCESS", null, 0);
    }

    /** 记录失败操作 */
    public void recordFailure(
            TenantId tenantId,
            Long userId,
            String ip,
            String action,
            String targetType,
            String targetId,
            Object detail,
            String errorMsg) {
        record(tenantId, userId, ip, action, targetType, targetId, detail, "FAILED", errorMsg, 0);
    }
}
