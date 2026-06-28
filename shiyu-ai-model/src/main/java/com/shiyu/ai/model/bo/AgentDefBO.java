package com.shiyu.ai.model.bo;

import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Agent 定义业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentDefBO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String agentId;
    private String name;
    private String description;
    private Long ownerId;
    private String currentVersion;
    private String status;
    private String delFlag;
}
