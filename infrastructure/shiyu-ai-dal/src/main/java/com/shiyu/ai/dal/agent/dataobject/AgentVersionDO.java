package com.shiyu.ai.dal.agent.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Agent 版本
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "agent_version")
public class AgentVersionDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String agentId;

    private String versionNumber;

    private String description;

    private String graphConfig;

    private String canvasConfig;

    /** 扩展字段：版本所有节点的入参定义 (JSON) */
    private String extInfo;
}
