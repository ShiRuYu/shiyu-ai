package com.shiyu.ai.agent.workflow.context;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class RecommendContext {
    private Long studentId;
    private List<Long> weakKnowledgeIds = new ArrayList<>();
    private List<String> weakKnowledgeNames = new ArrayList<>();
    private String recommendation;
    private String recommendType; // KNOWLEDGE / QUESTION / RESOURCE
}
