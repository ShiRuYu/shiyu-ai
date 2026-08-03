package com.shiyu.ai.education.agent.node.creator;

import com.shiyu.ai.education.agent.graph.AbilityQueryNode;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.creator.NodeCreator;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.knowledge.point.KnowledgePointService;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import org.springframework.stereotype.Component;

@Component
public class AbilityQueryNodeCreator implements NodeCreator {

    private final KnowledgePointService knowledgePointService;
    private final KnowledgeRelationService knowledgeRelationService;
    private final AbilityService abilityService;

    public AbilityQueryNodeCreator(KnowledgePointService knowledgePointService,
                                   KnowledgeRelationService knowledgeRelationService,
                                   AbilityService abilityService) {
        this.knowledgePointService = knowledgePointService;
        this.knowledgeRelationService = knowledgeRelationService;
        this.abilityService = abilityService;
    }

    @Override
    public NodeType getType() {
        return NodeType.ABILITY_QUERY;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        AbilityQueryNode node = new AbilityQueryNode(
                knowledgePointService, knowledgeRelationService, abilityService);
        node.setConfig(config);
        return node;
    }
}
