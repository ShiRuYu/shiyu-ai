package com.shiyu.ai.dal.dataobject.agent;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "agent_version")
public class AgentVersionDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String agentId;

    private String versionNumber;

    private Long workspaceId;

    private String description;

    private String status;

    private String graphConfig;

    private String canvasConfig;

    /** 扩展字段：版本所有节点的入参定义 (JSON) */
    private String extInfo;
    private String delFlag;
}
