package com.shiyu.ai.dal.bo.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent 版本业务对象
 */
@Data
public class AgentVersionBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String agentId;
    private String versionNumber;
    private String description;
    private String status;
    private String graphConfig;
    private String canvasConfig;
    /** 扩展字段：版本所有节点的入参定义 JSON */
    private String extInfo;
    private String delFlag;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
