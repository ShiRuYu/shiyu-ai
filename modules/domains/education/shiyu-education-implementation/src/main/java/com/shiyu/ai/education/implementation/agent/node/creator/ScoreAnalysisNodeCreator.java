package com.shiyu.ai.education.implementation.agent.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.education.implementation.agent.graph.ScoreAnalysisNode;
import com.shiyu.ai.education.implementation.application.AbilityService;

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
