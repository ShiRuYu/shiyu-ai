package com.shiyu.ai.agent.implementation.event.listener;
import com.shiyu.ai.agent.implementation.event.model.AuditEvent;

import com.shiyu.ai.agent.implementation.domain.model.AuditLogBO;
import com.shiyu.ai.agent.implementation.port.repository.AuditLogRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 处理 Audit 事件 相关事件或请求，并推进后续业务流程。
 */
@Slf4j
@Component
public class AuditEventListener {

    /**
     * auditLogRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuditLogRepository auditLogRepository;

    /**
     * 执行 Audit 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param auditLogRepository 用于完成本次业务处理的 auditLogRepository 参数。
     */
    public AuditEventListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 处理 Audit 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    @Async
    @EventListener
    public void onAuditEvent(AuditEvent event) {
        Objects.requireNonNull(event, "audit event must not be null");
        try {
            AuditLogBO record = new AuditLogBO();
            if (event.getTenantId() == null) {
                throw new IllegalArgumentException("audit event tenantId must not be null");
            }
            record.setTenantId(event.getTenantId().value());
            record.setUserId(event.getUserId());
            record.setAction(event.getAction());
            record.setTargetType(event.getTargetType());
            record.setTargetId(event.getTargetId());
            record.setDetail(event.getDetail());
            record.setIp(event.getIp());
            record.setResult(event.getResult());
            record.setErrorMsg(event.getErrorMsg());
            record.setDurationMs(event.getDurationMs());
            record.setCreateTime(LocalDateTime.now());
            auditLogRepository.insert(event.getTenantId(), record);
            log.debug(
                    "审计日志已记录: actionPresent={}, userIdPresent={}",
                    event.getAction() != null,
                    event.getUserId() != null);
        } catch (Exception e) {
            log.warn(
                    "写入审计日志失败: actionPresent={}, userIdPresent={}, errorType={},"
                            + " errorMessageLength={}",
                    event.getAction() != null,
                    event.getUserId() != null,
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }
    }
}
