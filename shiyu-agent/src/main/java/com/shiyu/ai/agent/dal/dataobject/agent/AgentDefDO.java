package com.shiyu.ai.agent.dal.dataobject.agent;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "agent_def")
public class AgentDefDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String agentId;

    private String name;

    private String description;

    private Long workspaceId;

    private String currentVersion;

    private String status;

    private String delFlag;
}
