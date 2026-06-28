package com.shiyu.ai.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class NodeConfigRequest {

    @NotBlank(message = "节点标识不能为空")
    private String nodeId;

    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    @NotBlank(message = "节点类型不能为空")
    private String nodeType;

    private String description;

    private Boolean enabled;

    private Long timeout;

    private Integer retryCount;

    private Long retryInterval;

    private String errorStrategy;

    private String logLevel;

    private Map<String, Object> properties;

    private Map<String, Object> config;
}
