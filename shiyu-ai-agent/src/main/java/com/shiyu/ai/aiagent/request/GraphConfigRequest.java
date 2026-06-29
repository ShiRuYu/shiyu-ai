package com.shiyu.ai.aiagent.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GraphConfigRequest {

    private String name;

    private String description;

    private String startNode;

    private String endNode;

    private Map<String, NodeConfigDTO> nodes;

    private Map<String, List<String>> edges;

    private Map<String, ConditionalEdgeDTO> conditionalEdges;

    @Data
    public static class NodeConfigDTO {
        private String nodeName;
        private String description;
        private String nodeType;
        private Boolean enabled;
        private Long timeout;
        private Integer retryCount;
        private Long retryInterval;
        private String errorStrategy;
        private String logLevel;
        private Map<String, Object> properties;
        private Map<String, Object> config;
    }

    @Data
    public static class ConditionalEdgeDTO {
        private String defaultTarget;
        private Map<String, String> nodeMappings;
        private String conditionType;
    }
}
