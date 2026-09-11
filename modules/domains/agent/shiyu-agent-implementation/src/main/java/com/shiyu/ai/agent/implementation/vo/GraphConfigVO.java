package com.shiyu.ai.agent.implementation.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphConfigVO {

    private String name;

    private String description;

    private String startNode;

    private String endNode;

    private Map<String, Object> nodes;

    private Map<String, Object> edges;

    private Map<String, Object> conditionalEdges;
}
