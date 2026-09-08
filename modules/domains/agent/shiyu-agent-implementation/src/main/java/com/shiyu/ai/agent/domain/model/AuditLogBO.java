package com.shiyu.ai.agent.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AuditLogBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;

    private Long tenantId;
    private Long userId;
    private String action;
    private String targetType;
    private String targetId;
    private String detail;
    private String ip;
    private String result;
    private String errorMsg;
    private Long durationMs;
    private LocalDateTime createTime;
}
