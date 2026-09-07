package com.shiyu.ai.agent.event;

import com.shiyu.ai.agent.domain.model.AuditLogBO;
import com.shiyu.ai.agent.port.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 审计事件监听器
 * <p>
 * 异步消费 {@link AuditEvent}，写入 {@code audit_log} 表。
 */
@Slf4j
@Component
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    public AuditEventListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

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
            log.debug("审计日志已记录: action={}, userId={}", event.getAction(), event.getUserId());
        } catch (Exception e) {
            log.warn("写入审计日志失败: action={}, userId={}", event.getAction(), event.getUserId(), e);
        }
    }
}
