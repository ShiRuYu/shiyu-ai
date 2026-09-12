package com.shiyu.ai.agent.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.agent.implementation.domain.model.AgentVersionBO;
import com.shiyu.ai.common.mybatis.model.TenantEntity;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Agent 版本 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table(value = "agent_version")
@AutoMapper(target = AgentVersionBO.class, reverseConvertGenerate = true)
public class AgentVersionDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String agentId;

    /**
     * versionNumber 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String versionNumber;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;

    /**
     * 图结构配置，表示当前对象中的对应属性。
     */
    private String graphConfig;

    /**
     * canvasConfig 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String canvasConfig;

    /** 扩展字段：版本所有节点的入参定义 (JSON) */
    private String extInfo;
}
