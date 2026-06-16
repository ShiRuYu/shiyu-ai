package com.shiyu.ai.agent.domain.bo;

import com.shiyu.ai.agent.dal.dataobject.agent.AgentVersionDO;
import com.shiyu.ai.common.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Agent 版本业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AgentVersionDO.class, reverseConvertGenerate = true)
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
