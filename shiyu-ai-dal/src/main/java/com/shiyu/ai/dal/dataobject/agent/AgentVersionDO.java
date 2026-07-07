package com.shiyu.ai.dal.dataobject.agent;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table(value = "agent_version")
public class AgentVersionDO implements Serializable {

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
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
