package com.shiyu.ai.dal.agent.bo;

import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.agent.dataobject.AgentDefDO;

/**
 * Agent 定义业务对象
 */
@AutoMapper(target = AgentDefDO.class, reverseConvertGenerate = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentDefBO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String agentId;
    private String name;
    private String description;
    private Long ownerId;
    private String currentVersion;
    /** 扩展字段：聚合的节点入参定义 JSON */
    private String extInfo;
}
