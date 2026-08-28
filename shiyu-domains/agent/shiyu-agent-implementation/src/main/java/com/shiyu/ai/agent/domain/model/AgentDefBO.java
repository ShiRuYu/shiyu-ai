package com.shiyu.ai.agent.domain.model;

import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Agent 定义业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentDefBO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private String agentId;
    private String name;
    private String description;
    private Long ownerId;
    private String currentVersion;
    /** 扩展字段：聚合的节点入参定义 JSON */
    private String extInfo;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}
