package com.shiyu.ai.education.agent.node.creator;

import com.shiyu.ai.education.agent.graph.AbilityQueryNode;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.creator.NodeCreator;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.knowledge.port.KnowledgePointPort;
import com.shiyu.ai.knowledge.port.KnowledgeRelationPort;
import org.springframework.stereotype.Component;

@Component
public class AbilityQueryNodeCreator implements NodeCreator {

    private final KnowledgePointPort knowledgePointService;
    private final KnowledgeRelationPort knowledgeRelationService;
    private final AbilityService abilityService;

    public AbilityQueryNodeCreator(KnowledgePointPort knowledgePointService,
                                   KnowledgeRelationPort knowledgeRelationService,
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
