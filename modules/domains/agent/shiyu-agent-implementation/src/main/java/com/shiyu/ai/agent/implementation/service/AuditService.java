package com.shiyu.ai.agent.implementation.service;

import com.shiyu.ai.agent.implementation.event.model.AuditEvent;
import com.shiyu.ai.agent.implementation.event.publisher.EventPublisher;
import com.shiyu.ai.agent.implementation.domain.model.AuditLogBO;
import com.shiyu.ai.agent.implementation.port.repository.AuditLogRepository;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
    /** 用于在业务事务中持久化审计记录的仓储。 */
    private final AuditLogRepository auditLogRepository;

    /**
     * 执行 Audit 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param eventPublisher 用于完成本次业务处理的 eventPublisher 参数。
     */
    public AuditService(EventPublisher eventPublisher) {
        this(eventPublisher, null);
    }

    /**
     * 创建兼顾尽力发布事件与持久化审计记录的服务。
     *
     * @param eventPublisher 普通审计事件发布器。
     * @param auditLogRepository 必须同步写入时使用的审计仓储。
     */
    @Autowired
    public AuditService(EventPublisher eventPublisher, AuditLogRepository auditLogRepository) {
        this.eventPublisher = eventPublisher;
        this.auditLogRepository = auditLogRepository;
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

    /**
     * 同步写入必须与业务状态变更共同提交的成功审计记录。
     *
     * @param tenantId 当前租户
     * @param userId 操作者
     * @param ip 请求来源地址
     * @param action 操作名称
     * @param targetType 目标类型
     * @param targetId 目标标识
     * @param detail 审计详情
     */
    public void recordSuccessRequired(
            TenantId tenantId,
            Long userId,
            String ip,
            String action,
            String targetType,
            String targetId,
            Object detail) {
        if (auditLogRepository == null) {
            throw new IllegalStateException("required audit repository is unavailable");
        }
        AuditLogBO record = new AuditLogBO();
        record.setTenantId(tenantId.value());
        record.setUserId(userId);
        record.setAction(action);
        record.setTargetType(targetType);
        record.setTargetId(targetId);
        record.setDetail(detail != null ? JSONUtils.toJsonString(detail) : null);
        record.setIp(ip);
        record.setResult("SUCCESS");
        record.setDurationMs(0L);
        record.setCreateTime(LocalDateTime.now());
        TenantScope.withTenant(
                tenantId,
                () -> {
                    auditLogRepository.insert(tenantId, record);
                    return null;
                });
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
