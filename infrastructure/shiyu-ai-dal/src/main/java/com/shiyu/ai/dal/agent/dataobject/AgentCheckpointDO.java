package com.shiyu.ai.dal.agent.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.shiyu.ai.agent.domain.model.AgentCheckpointBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("agent_checkpoint")
@AutoMapper(target = AgentCheckpointBO.class, reverseConvertGenerate = true)
public class AgentCheckpointDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String checkpointId;

    private String executionId;

    private String nodeId;

    private String stateData;

    private LocalDateTime createTime;
}
