package com.shiyu.ai.dal.bo.agent;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.dataobject.agent.AgentCheckpointDO;

@Data
@AutoMapper(target = AgentCheckpointDO.class, reverseConvertGenerate = true)
public class AgentCheckpointBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String checkpointId;

    private String executionId;

    private String nodeId;

    private String stateData;

    private LocalDateTime createTime;
}
