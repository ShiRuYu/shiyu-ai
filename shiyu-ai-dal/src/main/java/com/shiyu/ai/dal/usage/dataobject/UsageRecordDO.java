package com.shiyu.ai.dal.usage.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一用量记录
 * <p>
 * 通用字段由表内列承载，类型专属字段以 JSON 存放于 ext_info。
 * usage_type 目前支持: LLM / EMBEDDING
 * </p>
 */
@Data
@Table("agent_usage_record")
public class UsageRecordDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    /** 用量类型：LLM / EMBEDDING */
    private String usageType;

    /** 延迟（毫秒），所有类型通用 */
    private Long latencyMs;

    /** 用户 ID */
    private Long userId;

    /** 会话 ID */
    private String sessionId;

    /** 类型专属字段（JSON），如 LLM 的 platform/model/tokens/cost，EMBEDDING 的 textLength/vectorCount */
    private String extInfo;

    /** 创建时间 */
    private LocalDateTime createTime;
}
