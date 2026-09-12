package com.shiyu.ai.governance.implementation.usage.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.governance.implementation.usage.domain.model.UsageRecordBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一用量记录
 *
 * <p>通用字段由表内列承载，类型专属字段以 JSON 存放于 ext_info。 usage_type 目前支持: LLM / EMBEDDING
 */
@Data
@Table("governance_usage_record")
@AutoMapper(target = UsageRecordBO.class, reverseConvertGenerate = true)
public class UsageRecordDO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id private String id;

    /** 用量类型：LLM / EMBEDDING */
    private String usageType;

    /** 延迟（毫秒），所有类型通用 */
    private Long latencyMs;

    /** 用户 ID */
    private Long userId;

    /** 租户 ID，用于隔离用量与配额统计 */
    private Long tenantId;

    /**
     * 来源类型，表示当前对象中的对应属性。
     */
    private String sourceType;
    /**
     * 来源标识，表示当前对象中的对应属性。
     */
    private String sourceId;
    /**
     * correlationId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String correlationId;
    /**
     * inputTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long inputTokens;
    /**
     * outputTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long outputTokens;
    /**
     * 成本，表示当前对象中的对应属性。
     */
    private java.math.BigDecimal cost;
    /**
     * occurredAt 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime occurredAt;

    /** 会话 ID */
    private String sessionId;

    /** 类型专属字段（JSON），如 LLM 的 platform/model/tokens/cost，EMBEDDING 的 textLength/vectorCount */
    private String extInfo;

    /** 创建时间 */
    private LocalDateTime createTime;
}
