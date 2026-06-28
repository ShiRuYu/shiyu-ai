package com.shiyu.ai.aiagent;

import com.shiyu.ai.aiagent.graph.Graph;
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
    private Graph graph;
    private long createdAt;
}
