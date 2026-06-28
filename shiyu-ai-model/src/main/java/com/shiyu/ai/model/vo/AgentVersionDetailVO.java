package com.shiyu.ai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentVersionDetailVO {

    private Long id;

    private String agentId;

    private String versionNumber;

    private String description;

    private String status;

    private GraphConfigVO graphConfig;

    private String canvasConfig;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphConfigVO {
        private String name;
        private String description;
        private String startNode;
        private String endNode;
        private Map<String, Object> nodes;
        private Map<String, Object> edges;
        private Map<String, Object> conditionalEdges;
    }
}
