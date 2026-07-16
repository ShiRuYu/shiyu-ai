package com.shiyu.ai.agent.event;

import java.time.Instant;
import java.util.Map;

/**
 * 审计事件
 * <p>
 * 当关键操作发生时发布（登录、Agent 执行、模型调用、知识检索、CRUD 等），
 * 由 {@code AuditService} 异步消费后写入 {@code audit_log} 表。
 */
public class AuditEvent extends DomainEvent {

    private final Long userId;
    private final String action;
    private final String targetType;
    private final String targetId;
    private final String detail;
    private final String ip;
    private final String result;
    private final String errorMsg;
    private final long durationMs;

    public AuditEvent(Long userId, String action, String targetType, String targetId,
                      String detail, String ip, String result, String errorMsg, long durationMs) {
        super("AUDIT");
        this.userId = userId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.detail = detail;
        this.ip = ip;
        this.result = result;
        this.errorMsg = errorMsg;
        this.durationMs = durationMs;
    }

    public Long getUserId() { return userId; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getDetail() { return detail; }
    public String getIp() { return ip; }
    public String getResult() { return result; }
    public String getErrorMsg() { return errorMsg; }
    public long getDurationMs() { return durationMs; }
}
