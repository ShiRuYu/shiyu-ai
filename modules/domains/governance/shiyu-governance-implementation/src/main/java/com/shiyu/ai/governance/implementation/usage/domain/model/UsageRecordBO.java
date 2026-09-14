package com.shiyu.ai.governance.implementation.usage.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一用量记录
 *
 * <p>通用字段由表内列承载，类型专属字段以 JSON 存放于 ext_info。 usage_type 目前支持: LLM / EMBEDDING
 */
@Data
public class UsageRecordBO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private String id;

    /** 用量类型：LLM / EMBEDDING */
    private String usageType;

    /** 延迟（毫秒），所有类型通用 */
    private Long latencyMs;

    /** 用户 ID */
    private Long userId;

    /** 租户 ID，用于隔离用量与配额统计 */
    private Long tenantId;

    /** 产生用量的业务来源类型。 */
    private String sourceType;

    /** 产生用量的业务来源标识。 */
    private String sourceId;

    /** 用于幂等关联同一业务事件的关联标识。 */
    private String correlationId;

    /** 本次调用消耗的输入 token 数量。 */
    private Long inputTokens;

    /** 本次调用产生的输出 token 数量。 */
    private Long outputTokens;

    /** 本次调用产生的费用。 */
    private BigDecimal cost;

    /** 用量事件发生时间。 */
    private LocalDateTime occurredAt;

    /** 会话标识。 */
    private String sessionId;

    /** 类型专属字段（JSON），如 LLM 的 platform/model/tokens/cost，EMBEDDING 的 textLength/vectorCount */
    private String extInfo;

    /** 创建时间 */
    private LocalDateTime createTime;
}
