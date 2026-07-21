package com.shiyu.ai.dal.agent.bo;

import com.shiyu.ai.dal.agent.dataobject.AgentVersionDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent 版本业务对象
 */
@AutoMapper(target = AgentVersionDO.class, reverseConvertGenerate = true)
@Data
public class AgentVersionBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String agentId;
    private String versionNumber;
    private String description;
    private Integer status;
    private String statusDesc;
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
