package com.shiyu.ai.agent.request;
import lombok.Data;
@Data
public class IntentDefRequest {
    private String agentId;
    private String code;
    private String name;
    private String description;
    private String category;
    private Integer priority;
    private Double confidenceThreshold;
    private String targetNode;
    private Integer status;
}
