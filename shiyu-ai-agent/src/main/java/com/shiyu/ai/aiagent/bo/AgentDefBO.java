package com.shiyu.ai.aiagent.bo;

import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.dataobject.agent.AgentDefDO;

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
    private String status;
    private String delFlag;
}
