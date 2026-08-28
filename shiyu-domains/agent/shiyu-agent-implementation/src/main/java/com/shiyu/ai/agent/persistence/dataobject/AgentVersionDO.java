package com.shiyu.ai.agent.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.shiyu.ai.agent.domain.model.AgentVersionBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * Agent 版本
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table(value = "agent_version")
@AutoMapper(target = AgentVersionBO.class, reverseConvertGenerate = true)
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
