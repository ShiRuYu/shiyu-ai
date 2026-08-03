package com.shiyu.ai.education.agent.node.creator;

import com.shiyu.ai.education.agent.graph.ScoreAnalysisNode;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.creator.NodeCreator;
import com.shiyu.ai.education.service.AbilityService;
import org.springframework.stereotype.Component;

@Component
public class ScoreAnalysisNodeCreator implements NodeCreator {

    private final AbilityService abilityService;

    public ScoreAnalysisNodeCreator(AbilityService abilityService) {
        this.abilityService = abilityService;
    }

    @Override
    public NodeType getType() {
        return NodeType.SCORE_ANALYSIS;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        ScoreAnalysisNode node = new ScoreAnalysisNode(abilityService);
        node.setConfig(config);
        return node;
    }
}
