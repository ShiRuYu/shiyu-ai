package com.shiyu.ai.aiagent.bo;

import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.dataobject.agent.AgentVersionDO;

/**
 * Agent 版本业务对象
 */
@AutoMapper(target = AgentVersionDO.class, reverseConvertGenerate = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentVersionBO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String agentId;
    private String versionNumber;
    private String description;
    private String status;
    private String graphConfig;
    private String canvasConfig;
    private String delFlag;
}
