package com.shiyu.ai.agent.event;

import com.shiyu.ai.common.core.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 审计事件监听器
 * <p>
 * 异步消费 {@link AuditEvent}，写入 {@code audit_log} 表。
 */
@Slf4j
@Component
public class AuditEventListener {

    private final JdbcTemplate jdbcTemplate;

    public AuditEventListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Async
    @EventListener
    public void onAuditEvent(AuditEvent event) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO audit_log (user_id, action, target_type, target_id, detail, ip, result, error_msg, duration_ms, create_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    event.getUserId(), event.getAction(), event.getTargetType(), event.getTargetId(),
                    event.getDetail(), event.getIp(), event.getResult(), event.getErrorMsg(),
                    event.getDurationMs(), LocalDateTime.now());
            log.debug("审计日志已记录: action={}, userId={}", event.getAction(), event.getUserId());
        } catch (Exception e) {
            log.warn("写入审计日志失败: action={}, userId={}", event.getAction(), event.getUserId(), e);
        }
    }
}
