package com.shiyu.ai.governance.implementation.usage.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 统一用量记录
 * <p>
 * 通用字段由表内列承载，类型专属字段以 JSON 存放于 ext_info。
 * usage_type 目前支持: LLM / EMBEDDING
 * </p>
 */
@Data
public class UsageRecordBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private String id;

    /** 用量类型：LLM / EMBEDDING */
    private String usageType;

    /** 延迟（毫秒），所有类型通用 */
    private Long latencyMs;

    /** 用户 ID */
    private Long userId;

    /** 租户 ID，用于隔离用量与配额统计 */
    private Long tenantId;

    /** Idempotent business source identity owned by Governance. */
    private String sourceType;
    private String sourceId;
    private String correlationId;
    private Long inputTokens;
    private Long outputTokens;
    private BigDecimal cost;
    private LocalDateTime occurredAt;

    /** 会话 ID */
    private String sessionId;

    /** 类型专属字段（JSON），如 LLM 的 platform/model/tokens/cost，EMBEDDING 的 textLength/vectorCount */
    private String extInfo;

    /** 创建时间 */
    private LocalDateTime createTime;
}
