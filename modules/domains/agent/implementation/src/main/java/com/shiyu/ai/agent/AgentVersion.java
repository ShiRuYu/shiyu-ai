package com.shiyu.ai.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shiyu.ai.agent.graph.Graph;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentVersion {
    private String versionNumber;
    private String description;
    @JsonIgnore
    private Graph graph;
    private long createdAt;
}
