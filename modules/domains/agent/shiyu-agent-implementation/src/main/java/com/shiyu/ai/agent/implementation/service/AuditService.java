package com.shiyu.ai.agent.implementation.service;

import com.shiyu.ai.agent.implementation.event.model.AuditEvent;
import com.shiyu.ai.agent.implementation.event.publisher.EventPublisher;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.kernel.context.TenantId;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * 审计日志服务
 *
 * <p>提供统一的审计记录入口，支持手动记录和通过 AuditEvent 异步消费。
 */
@Slf4j
@Service
public class AuditService {

    /**
     * eventPublisher 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EventPublisher eventPublisher;

    /**
     * {@code AuditService} 创建并初始化当前类型实例。
     *
     * @param eventPublisher 参数值，用于执行当前操作。
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
