package com.shiyu.ai.dal.agent.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import com.shiyu.ai.agent.domain.model.AgentDefBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "agent_def")
@AutoMapper(target = AgentDefBO.class, reverseConvertGenerate = true)
public class AgentDefDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String agentId;

    private String name;

    private String description;

    private String currentVersion;

    private String extInfo;
}
