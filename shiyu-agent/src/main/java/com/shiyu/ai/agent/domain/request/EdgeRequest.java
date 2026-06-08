package com.shiyu.ai.agent.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class EdgeRequest {

    @NotBlank(message = "源节点不能为空")
    private String sourceNodeId;

    @NotBlank(message = "目标节点不能为空")
    private String targetNodeId;

    private String edgeType;

    private Map<String, String> conditionMappings;

    private String defaultTarget;

    private String conditionType;
}
